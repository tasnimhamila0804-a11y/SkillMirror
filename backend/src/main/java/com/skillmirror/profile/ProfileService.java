package com.skillmirror.profile;

import com.skillmirror.common.ApiException;
import com.skillmirror.profile.dto.ProfileDtos.CertificateDto;
import com.skillmirror.profile.dto.ProfileDtos.CertificateRequest;
import com.skillmirror.profile.dto.ProfileDtos.ExperienceDto;
import com.skillmirror.profile.dto.ProfileDtos.ExperienceRequest;
import com.skillmirror.profile.dto.ProfileDtos.ProfileDto;
import com.skillmirror.profile.dto.ProfileDtos.ProjectDto;
import com.skillmirror.profile.dto.ProfileDtos.ProjectRequest;
import com.skillmirror.profile.dto.ProfileDtos.UpdateProfileRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Professional profile and portfolio use cases. */
@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ExperienceRepository experienceRepository;
    private final CertificateRepository certificateRepository;
    private final UserProjectRepository projectRepository;

    public ProfileService(ProfileRepository profileRepository, ExperienceRepository experienceRepository,
                          CertificateRepository certificateRepository, UserProjectRepository projectRepository) {
        this.profileRepository = profileRepository;
        this.experienceRepository = experienceRepository;
        this.certificateRepository = certificateRepository;
        this.projectRepository = projectRepository;
    }

    public Profile requireEntity(Long userId) {
        return profileRepository.findByUserId(userId).orElseGet(() -> {
            Profile profile = new Profile();
            profile.setUserId(userId);
            return profileRepository.save(profile);
        });
    }

    public ProfileDto get(Long userId) {
        return ProfileDto.from(requireEntity(userId));
    }

    @Transactional
    public ProfileDto update(Long userId, UpdateProfileRequest request) {
        Profile profile = requireEntity(userId);
        if (request.situation() != null) profile.setSituation(trimTo(request.situation(), 60));
        if (request.domain() != null) profile.setDomain(trimTo(request.domain(), 160));
        if (request.educationLevel() != null) profile.setEducationLevel(trimTo(request.educationLevel(), 120));
        if (request.experienceSummary() != null) profile.setExperienceSummary(trimTo(request.experienceSummary(), 2000));
        if (request.targetJob() != null) profile.setTargetJob(trimTo(request.targetJob(), 160));
        if (request.careerGoal() != null) profile.setCareerGoal(trimTo(request.careerGoal(), 60));
        if (request.targetCompany() != null) profile.setTargetCompany(trimTo(request.targetCompany(), 160));
        if (request.positionType() != null) profile.setPositionType(trimTo(request.positionType(), 40));
        if (request.hoursPerWeek() != null) profile.setHoursPerWeek(request.hoursPerWeek());
        if (request.learningPreference() != null) profile.setLearningPreference(trimTo(request.learningPreference(), 40));
        if (request.preferredDifficulty() != null) profile.setPreferredDifficulty(trimTo(request.preferredDifficulty(), 30));
        if (request.deadline() != null) profile.setDeadline(trimTo(request.deadline(), 60));
        if (request.headline() != null) profile.setHeadline(trimTo(request.headline(), 220));
        if (request.bio() != null) profile.setBio(trimTo(request.bio(), 4000));
        if (request.githubUrl() != null) profile.setGithubUrl(trimTo(request.githubUrl(), 300));
        if (request.portfolioUrl() != null) profile.setPortfolioUrl(trimTo(request.portfolioUrl(), 300));
        profile.touch();
        return ProfileDto.from(profileRepository.save(profile));
    }

    // --- Experiences -------------------------------------------------------

    public List<ExperienceDto> experiences(Long userId) {
        return experienceRepository.findByUserIdOrderByIdDesc(userId).stream().map(ExperienceDto::from).toList();
    }

    @Transactional
    public ExperienceDto addExperience(Long userId, ExperienceRequest request) {
        Experience entity = new Experience();
        entity.setUserId(userId);
        apply(entity, request);
        return ExperienceDto.from(experienceRepository.save(entity));
    }

    @Transactional
    public ExperienceDto updateExperience(Long userId, Long id, ExperienceRequest request) {
        Experience entity = experienceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Expérience introuvable."));
        apply(entity, request);
        entity.touch();
        return ExperienceDto.from(experienceRepository.save(entity));
    }

    @Transactional
    public void deleteExperience(Long userId, Long id) {
        Experience entity = experienceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Expérience introuvable."));
        experienceRepository.delete(entity);
    }

    // --- Certificates ------------------------------------------------------

    public List<CertificateDto> certificates(Long userId) {
        return certificateRepository.findByUserIdOrderByIdDesc(userId).stream().map(CertificateDto::from).toList();
    }

    @Transactional
    public CertificateDto addCertificate(Long userId, CertificateRequest request) {
        Certificate entity = new Certificate();
        entity.setUserId(userId);
        apply(entity, request);
        return CertificateDto.from(certificateRepository.save(entity));
    }

    @Transactional
    public CertificateDto updateCertificate(Long userId, Long id, CertificateRequest request) {
        Certificate entity = certificateRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Certification introuvable."));
        apply(entity, request);
        return CertificateDto.from(certificateRepository.save(entity));
    }

    @Transactional
    public void deleteCertificate(Long userId, Long id) {
        Certificate entity = certificateRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Certification introuvable."));
        certificateRepository.delete(entity);
    }

    // --- Projects ----------------------------------------------------------

    public List<ProjectDto> projects(Long userId) {
        return projectRepository.findByUserIdOrderByIdDesc(userId).stream().map(ProjectDto::from).toList();
    }

    @Transactional
    public ProjectDto addProject(Long userId, ProjectRequest request) {
        UserProject entity = new UserProject();
        entity.setUserId(userId);
        apply(entity, request);
        return ProjectDto.from(projectRepository.save(entity));
    }

    @Transactional
    public ProjectDto updateProject(Long userId, Long id, ProjectRequest request) {
        UserProject entity = projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Projet introuvable."));
        apply(entity, request);
        entity.touch();
        return ProjectDto.from(projectRepository.save(entity));
    }

    @Transactional
    public void deleteProject(Long userId, Long id) {
        UserProject entity = projectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Projet introuvable."));
        projectRepository.delete(entity);
    }

    // --- helpers -----------------------------------------------------------

    public List<Experience> rawExperiences(Long userId) {
        return experienceRepository.findByUserIdOrderByIdDesc(userId);
    }

    public List<Certificate> rawCertificates(Long userId) {
        return certificateRepository.findByUserIdOrderByIdDesc(userId);
    }

    public List<UserProject> rawProjects(Long userId) {
        return projectRepository.findByUserIdOrderByIdDesc(userId);
    }

    private void apply(Experience entity, ExperienceRequest request) {
        entity.setTitle(trimTo(request.title(), 190));
        entity.setOrganization(trimTo(request.organization(), 190));
        entity.setType(request.type() == null || request.type().isBlank() ? "INTERNSHIP" : request.type().trim());
        entity.setDescription(trimTo(request.description(), 4000));
        entity.setStartDate(trimTo(request.startDate(), 30));
        entity.setEndDate(trimTo(request.endDate(), 30));
        entity.setCurrent(Boolean.TRUE.equals(request.current()));
    }

    private void apply(Certificate entity, CertificateRequest request) {
        entity.setName(trimTo(request.name(), 190));
        entity.setIssuer(trimTo(request.issuer(), 190));
        entity.setIssuedDate(trimTo(request.issuedDate(), 30));
        entity.setUrl(trimTo(request.url(), 400));
    }

    private void apply(UserProject entity, ProjectRequest request) {
        entity.setName(trimTo(request.name(), 190));
        entity.setDescription(trimTo(request.description(), 4000));
        entity.setRepositoryUrl(trimTo(request.repositoryUrl(), 400));
        entity.setDemoUrl(trimTo(request.demoUrl(), 400));
        entity.setTechnologies(trimTo(request.technologies(), 600));
        entity.setStartedDate(trimTo(request.startedDate(), 30));
        entity.setEndedDate(trimTo(request.endedDate(), 30));
    }

    private String trimTo(String value, int max) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}
