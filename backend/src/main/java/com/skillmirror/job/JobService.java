package com.skillmirror.job;

import com.skillmirror.ai.AiService;
import com.skillmirror.ai.dto.AiDtos.ExtractionResult;
import com.skillmirror.ai.dto.AiDtos.ExtractedSkill;
import com.skillmirror.common.ApiException;
import com.skillmirror.job.dto.JobDtos.ConfirmSkillsRequest;
import com.skillmirror.job.dto.JobDtos.ExtractResponse;
import com.skillmirror.job.dto.JobDtos.JobOfferDto;
import com.skillmirror.job.dto.JobDtos.JobSkillDto;
import com.skillmirror.job.dto.JobDtos.JobSummaryDto;
import com.skillmirror.job.dto.JobDtos.ReviewedSkill;
import com.skillmirror.job.dto.JobDtos.SaveJobRequest;
import com.skillmirror.job.dto.JobDtos.UpdateJobRequest;
import com.skillmirror.skill.Skill;
import com.skillmirror.skill.SkillRepository;
import com.skillmirror.skill.SkillService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Job offer use cases including AI extraction and user review of extracted skills. */
@Service
public class JobService {

    private final JobOfferRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final SkillRepository skillRepository;
    private final AiService aiService;

    public JobService(JobOfferRepository jobRepository, JobSkillRepository jobSkillRepository,
                      SkillRepository skillRepository, AiService aiService) {
        this.jobRepository = jobRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.skillRepository = skillRepository;
        this.aiService = aiService;
    }

    public List<JobSummaryDto> list(Long userId) {
        return jobRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(job -> new JobSummaryDto(job.getId(), job.getTitle(), job.getCompany(),
                        job.getExtractionStatus(), job.getExperienceLevel(),
                        jobSkillRepository.findByJobOfferIdOrderById(job.getId()).size(),
                        job.getCreatedAt()))
                .toList();
    }

    public JobOfferDto get(Long userId, Long id) {
        return toDto(require(userId, id));
    }

    @Transactional
    public JobOfferDto create(Long userId, SaveJobRequest request) {
        JobOffer job = new JobOffer();
        job.setUserId(userId);
        apply(job, request.title(), request.company(), request.description(),
                request.experienceLevel(), request.educationRequirement());
        return toDto(jobRepository.save(job));
    }

    @Transactional
    public JobOfferDto update(Long userId, Long id, UpdateJobRequest request) {
        JobOffer job = require(userId, id);
        apply(job, request.title(), request.company(), request.description(),
                request.experienceLevel(), request.educationRequirement());
        job.touch();
        return toDto(jobRepository.save(job));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        JobOffer job = require(userId, id);
        jobSkillRepository.deleteByJobOfferId(job.getId());
        jobRepository.delete(job);
    }

    /** Runs the AI extraction and stores the proposed skills for review (never auto-confirmed). */
    @Transactional
    public ExtractResponse extract(Long userId, Long id) {
        JobOffer job = require(userId, id);
        ExtractionResult result = aiService.extractJobSkills(job.getDescription(), userId);

        jobSkillRepository.deleteByJobOfferId(job.getId());
        List<JobSkill> saved = new ArrayList<>();
        for (ExtractedSkill skill : merge(result.technical(), result.softSkills())) {
            saved.add(createJobSkill(job.getId(), skill.name(), skill.category(), skill.requirement(),
                    skill.weight(), skill.requiredLevel(), "AI", false));
        }
        jobSkillRepository.saveAll(saved);

        if (job.getExperienceLevel() == null && result.experienceLevel() != null) {
            job.setExperienceLevel(result.experienceLevel());
        }
        if (job.getEducationRequirement() == null && result.educationRequirement() != null) {
            job.setEducationRequirement(result.educationRequirement());
        }
        job.setExtractionStatus("EXTRACTED");
        job.touch();
        JobOffer updated = jobRepository.save(job);
        return new ExtractResponse(toDto(updated), result.simulated());
    }

    /** Persists the user's reviewed/corrected skill list — the basis of any analysis. */
    @Transactional
    public JobOfferDto confirmSkills(Long userId, Long id, ConfirmSkillsRequest request) {
        JobOffer job = require(userId, id);
        List<ReviewedSkill> skills = request.skills() == null ? List.of() : request.skills();
        List<ReviewedSkill> valid = skills.stream()
                .filter(s -> s.name() != null && !s.name().isBlank())
                .toList();
        if (valid.isEmpty()) {
            throw ApiException.badRequest("SKILLS_REQUIRED",
                    "Ajoutez au moins une compétence avant de lancer l'analyse.");
        }
        jobSkillRepository.deleteByJobOfferId(job.getId());
        List<JobSkill> saved = new ArrayList<>();
        for (ReviewedSkill reviewed : valid) {
            String requirement = normalizeRequirement(reviewed.requirement());
            int weight = reviewed.weight() != null
                    ? Math.max(1, Math.min(3, reviewed.weight()))
                    : ("DESIRED".equals(requirement) ? 1 : 3);
            saved.add(createJobSkill(job.getId(), reviewed.name(), reviewed.category(), requirement, weight,
                    reviewed.requiredLevel(), "MANUAL", true));
        }
        jobSkillRepository.saveAll(saved);
        job.setExtractionStatus("CONFIRMED");
        job.touch();
        return toDto(jobRepository.save(job));
    }

    public List<JobSkill> rawJobSkills(Long jobId) {
        return jobSkillRepository.findByJobOfferIdOrderById(jobId);
    }

    // --- helpers --------------------------------------------------------------

    private JobOffer require(Long userId, Long id) {
        return jobRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Offre d'emploi introuvable."));
    }

    private void apply(JobOffer job, String title, String company, String description,
                       String experienceLevel, String educationRequirement) {
        job.setTitle(truncate(title.trim(), 190));
        job.setCompany(company == null || company.isBlank() ? null : truncate(company.trim(), 190));
        String text = description == null ? "" : description.trim();
        if (text.length() > 30000) text = text.substring(0, 30000);
        job.setDescription(text);
        job.setExperienceLevel(experienceLevel == null || experienceLevel.isBlank()
                ? null : truncate(experienceLevel.trim(), 40));
        job.setEducationRequirement(educationRequirement == null || educationRequirement.isBlank()
                ? null : truncate(educationRequirement.trim(), 190));
    }

    private JobSkill createJobSkill(Long jobId, String name, String category, String requirement,
                                    int weight, String requiredLevel, String source, boolean confirmed) {
        JobSkill jobSkill = new JobSkill();
        jobSkill.setJobOfferId(jobId);
        jobSkill.setSkillName(truncate(name.trim(), 120));
        String normalized = Skill.normalize(name);
        jobSkill.setNormalizedName(truncate(normalized, 140));
        jobSkill.setCategory(SkillService.parseCategory(category).name());
        jobSkill.setRequirement(normalizeRequirement(requirement));
        jobSkill.setWeight(Math.max(1, Math.min(3, weight)));
        jobSkill.setRequiredLevel(normalizeLevel(requiredLevel));
        jobSkill.setSource(source);
        jobSkill.setConfirmed(confirmed);
        skillRepository.findByNormalizedName(normalized)
                .ifPresent(reference -> jobSkill.setSkillId(reference.getId()));
        return jobSkill;
    }

    private List<ExtractedSkill> merge(List<ExtractedSkill> technical, List<ExtractedSkill> soft) {
        List<ExtractedSkill> all = new ArrayList<>();
        if (technical != null) all.addAll(technical);
        if (soft != null) {
            for (ExtractedSkill skill : soft) {
                boolean duplicate = all.stream().anyMatch(
                        t -> Skill.normalize(t.name()).equals(Skill.normalize(skill.name())));
                if (!duplicate) all.add(skill);
            }
        }
        return all;
    }

    private String normalizeRequirement(String raw) {
        if (raw == null || raw.isBlank()) return "REQUIRED";
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        return upper.equals("DESIRED") ? "DESIRED" : "REQUIRED";
    }

    private String normalizeLevel(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        return switch (upper) {
            case "BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT" -> upper;
            default -> null;
        };
    }

    public JobOfferDto toDto(JobOffer job) {
        List<JobSkillDto> skills = jobSkillRepository.findByJobOfferIdOrderById(job.getId()).stream()
                .map(s -> new JobSkillDto(s.getId(), s.getSkillId(), s.getSkillName(), s.getCategory(),
                        s.getRequirement(), s.getWeight(), s.getRequiredLevel(), s.getSource(), s.isConfirmed()))
                .toList();
        return new JobOfferDto(job.getId(), job.getTitle(), job.getCompany(), job.getDescription(),
                job.getExperienceLevel(), job.getEducationRequirement(), job.getExtractionStatus(),
                job.getCreatedAt(), job.getUpdatedAt(), skills);
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
