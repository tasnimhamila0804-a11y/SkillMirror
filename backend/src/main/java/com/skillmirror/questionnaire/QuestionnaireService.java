package com.skillmirror.questionnaire;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.skillmirror.common.ApiException;
import com.skillmirror.profile.Profile;
import com.skillmirror.profile.ProfileRepository;
import com.skillmirror.profile.ProfileService;
import com.skillmirror.profile.dto.ProfileDtos.CertificateRequest;
import com.skillmirror.profile.dto.ProfileDtos.ExperienceRequest;
import com.skillmirror.profile.dto.ProfileDtos.ProfileDto;
import com.skillmirror.profile.dto.ProfileDtos.ProjectRequest;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.CompleteResponse;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.QuestionnaireDefinition;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.ResponseDto;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.SaveRequest;
import com.skillmirror.questionnaire.dto.QuestionnaireDtos.StepDef;
import com.skillmirror.skill.Skill;
import com.skillmirror.skill.SkillLevel;
import com.skillmirror.skill.SkillService;
import com.skillmirror.skill.SkillStatus;
import com.skillmirror.skill.UserSkill;
import com.skillmirror.skill.UserSkillRepository;
import com.skillmirror.user.User;
import com.skillmirror.user.UserRepository;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Initial profiling questionnaire: definition, draft saving, and profile creation on completion. */
@Service
public class QuestionnaireService {

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireService.class);

    private final QuestionnaireResponseRepository repository;
    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final SkillService skillService;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public QuestionnaireService(QuestionnaireResponseRepository repository, ProfileService profileService,
                                ProfileRepository profileRepository, SkillService skillService,
                                UserSkillRepository userSkillRepository, UserRepository userRepository,
                                ObjectMapper objectMapper) {
        this.repository = repository;
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.skillService = skillService;
        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public QuestionnaireDefinition definition() {
        return new QuestionnaireDefinition(
                "See your skills. Unlock your potential.",
                List.of(
                        new StepDef("situation", 1, "Votre situation actuelle",
                                "Où en êtes-vous aujourd'hui dans votre parcours ?",
                                List.of("situation", "domain", "educationLevel", "experienceSummary")),
                        new StepDef("skills", 2, "Vos compétences",
                                "Sélectionnez les compétences que vous possédez dans chaque catégorie.",
                                List.of("skills")),
                        new StepDef("self-assessment", 3, "Votre autoévaluation",
                                "Indiquez votre niveau pour chaque compétence sélectionnée.",
                                List.of("skills.level")),
                        new StepDef("experience", 4, "Expériences et preuves",
                                "Projets, stages, certifications, liens — tout est facultatif.",
                                List.of("projects", "experiences", "certificates", "githubUrl", "portfolioUrl")),
                        new StepDef("objective", 5, "Votre objectif professionnel",
                                "Quel métier visez-vous et dans quel contexte ?",
                                List.of("targetJob", "careerGoal", "targetCompany", "positionType")),
                        new StepDef("preferences", 6, "Vos préférences d'apprentissage",
                                "Nous adaptons vos projets à votre rythme.",
                                List.of("hoursPerWeek", "learningPreference", "preferredDifficulty", "deadline"))));
    }

    public ResponseDto get(Long userId) {
        QuestionnaireResponse response = repository.findByUserId(userId).orElse(null);
        if (response == null) {
            return new ResponseDto(null, 1, false, null, null);
        }
        return toDto(response);
    }

    @Transactional
    public ResponseDto save(Long userId, SaveRequest request) {
        QuestionnaireResponse response = repository.findByUserId(userId).orElseGet(() -> {
            QuestionnaireResponse created = new QuestionnaireResponse();
            created.setUserId(userId);
            return created;
        });
        if (request.currentStep() != null) {
            response.setCurrentStep(Math.max(1, Math.min(6, request.currentStep())));
        }
        if (request.payload() != null && !request.payload().isNull()) {
            response.setPayload(write(request.payload()));
        }
        response.touch();
        return toDto(repository.save(response));
    }

    @Transactional
    public CompleteResponse complete(Long userId, JsonNode payload) {
        if (payload == null || payload.isNull()) {
            throw ApiException.badRequest("PAYLOAD_REQUIRED", "Les réponses du questionnaire sont requises.");
        }
        JsonNode situation = payload.path("situation");
        if (situation.isMissingNode() || situation.isNull()
                || text(situation, "situation").isBlank()) {
            throw ApiException.badRequest("SITUATION_REQUIRED",
                    "Renseignez votre situation actuelle pour terminer le profilage.");
        }
        JsonNode skills = payload.path("skills");
        if (!skills.isArray() || skills.isEmpty()) {
            throw ApiException.badRequest("SKILLS_REQUIRED",
                    "Sélectionnez au moins une compétence pour terminer le profilage.");
        }

        Profile profile = profileService.requireEntity(userId);
        profile.setSituation(trimTo(text(situation, "situation"), 60));
        profile.setDomain(trimTo(text(situation, "domain"), 160));
        profile.setEducationLevel(trimTo(text(situation, "educationLevel"), 120));
        profile.setExperienceSummary(trimTo(text(situation, "experienceSummary"), 2000));

        JsonNode objective = payload.path("objective");
        if (!objective.isMissingNode() && !objective.isNull()) {
            profile.setTargetJob(trimTo(text(objective, "targetJob"), 160));
            profile.setCareerGoal(trimTo(text(objective, "careerGoal"), 60));
            profile.setTargetCompany(trimTo(text(objective, "targetCompany"), 160));
            profile.setPositionType(trimTo(text(objective, "positionType"), 40));
        }
        JsonNode preferences = payload.path("preferences");
        if (!preferences.isMissingNode() && !preferences.isNull()) {
            if (preferences.hasNonNull("hoursPerWeek")) {
                profile.setHoursPerWeek(preferences.path("hoursPerWeek").asInt(0) > 0
                        ? preferences.path("hoursPerWeek").asInt() : null);
            }
            profile.setLearningPreference(trimTo(text(preferences, "learningPreference"), 40));
            profile.setPreferredDifficulty(trimTo(text(preferences, "preferredDifficulty"), 30));
            profile.setDeadline(trimTo(text(preferences, "deadline"), 60));
        }

        int experiences = 0;
        int projects = 0;
        int certificates = 0;
        JsonNode experience = payload.path("experience");
        if (!experience.isMissingNode() && !experience.isNull()) {
            profile.setGithubUrl(trimTo(text(experience, "githubUrl"), 300));
            profile.setPortfolioUrl(trimTo(text(experience, "portfolioUrl"), 300));
            projects = importProjects(userId, experience.path("projects"));
            experiences = importExperiences(userId, experience.path("experiences"));
            certificates = importCertificates(userId, experience.path("certificates"));
        }
        profile.touch();
        profileRepository.save(profile);

        int skillCount = importSkills(userId, skills);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable."));
        user.setProfileCompleted(true);
        userRepository.save(user);

        QuestionnaireResponse response = repository.findByUserId(userId).orElseGet(() -> {
            QuestionnaireResponse created = new QuestionnaireResponse();
            created.setUserId(userId);
            return created;
        });
        response.setCurrentStep(6);
        response.setCompleted(true);
        response.setPayload(write(payload));
        response.touch();
        repository.save(response);

        return new CompleteResponse(ProfileDto.from(profile), skillCount, experiences, projects, certificates, true);
    }

    // --- import helpers ----------------------------------------------------

    private int importSkills(Long userId, JsonNode skills) {
        int count = 0;
        for (JsonNode node : skills) {
            String name = node.path("name").asText("").trim();
            if (name.isEmpty()) continue;
            Skill skill = skillService.resolveOrCreate(name, node.path("category").asText(null), userId);
            UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skill.getId()).orElse(null);
            SkillLevel level = SkillLevel.from(node.path("level").asText(null));
            if (userSkill == null) {
                userSkill = new UserSkill();
                userSkill.setUserId(userId);
                userSkill.setSkillId(skill.getId());
                userSkill.setSkillName(skill.getName());
                userSkill.setNormalizedName(skill.getNormalizedName());
                userSkill.setCategory(skill.getCategory().name());
                userSkill.setLevel(level);
                userSkill.setStatus(SkillStatus.DECLARED);
            } else if (level != SkillLevel.UNKNOWN || !node.path("level").isMissingNode()) {
                if (userSkill.getStatus() != SkillStatus.DEMONSTRATED) {
                    userSkill.setLevel(level);
                }
                userSkill.touch();
            }
            userSkillRepository.save(userSkill);
            count++;
        }
        return count;
    }

    private int importProjects(Long userId, JsonNode nodes) {
        if (!nodes.isArray()) return 0;
        var existing = new java.util.HashSet<String>();
        profileService.rawProjects(userId).forEach(p -> existing.add(p.getName().toLowerCase(Locale.ROOT)));
        int count = 0;
        for (JsonNode node : nodes) {
            String name = node.path("name").asText("").trim();
            if (name.isEmpty() || existing.contains(name.toLowerCase(Locale.ROOT))) continue;
            profileService.addProject(userId, new ProjectRequest(
                    name,
                    node.path("description").asText(null),
                    node.path("repositoryUrl").asText(null),
                    node.path("demoUrl").asText(null),
                    node.path("technologies").asText(null),
                    node.path("startedDate").asText(null),
                    node.path("endedDate").asText(null)));
            existing.add(name.toLowerCase(Locale.ROOT));
            count++;
        }
        return count;
    }

    private int importExperiences(Long userId, JsonNode nodes) {
        if (!nodes.isArray()) return 0;
        var existing = new java.util.HashSet<String>();
        profileService.rawExperiences(userId).forEach(e -> existing.add(e.getTitle().toLowerCase(Locale.ROOT)));
        int count = 0;
        for (JsonNode node : nodes) {
            String title = node.path("title").asText("").trim();
            if (title.isEmpty() || existing.contains(title.toLowerCase(Locale.ROOT))) continue;
            profileService.addExperience(userId, new ExperienceRequest(
                    title,
                    node.path("organization").asText(null),
                    node.path("type").asText(null),
                    node.path("description").asText(null),
                    node.path("startDate").asText(null),
                    node.path("endDate").asText(null),
                    node.path("current").asBoolean(false)));
            existing.add(title.toLowerCase(Locale.ROOT));
            count++;
        }
        return count;
    }

    private int importCertificates(Long userId, JsonNode nodes) {
        if (!nodes.isArray()) return 0;
        var existing = new java.util.HashSet<String>();
        profileService.rawCertificates(userId).forEach(c -> existing.add(c.getName().toLowerCase(Locale.ROOT)));
        int count = 0;
        for (JsonNode node : nodes) {
            String name = node.path("name").asText("").trim();
            if (name.isEmpty() || existing.contains(name.toLowerCase(Locale.ROOT))) continue;
            profileService.addCertificate(userId, new CertificateRequest(
                    name,
                    node.path("issuer").asText(null),
                    node.path("issuedDate").asText(null),
                    node.path("url").asText(null)));
            existing.add(name.toLowerCase(Locale.ROOT));
            count++;
        }
        return count;
    }

    // --- misc --------------------------------------------------------------

    private ResponseDto toDto(QuestionnaireResponse response) {
        JsonNode payload = null;
        if (response.getPayload() != null && !response.getPayload().isBlank()) {
            try {
                payload = objectMapper.readTree(response.getPayload());
            } catch (JsonProcessingException ex) {
                log.warn("Stored questionnaire payload is not valid JSON for user {}", response.getUserId());
                payload = NullNode.getInstance();
            }
        }
        return new ResponseDto(response.getId(), response.getCurrentStep(), response.isCompleted(), payload,
                response.getUpdatedAt());
    }

    private String write(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw ApiException.badRequest("INVALID_PAYLOAD", "Réponses de questionnaire invalides.");
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText("").trim();
    }

    private String trimTo(String value, int max) {
        if (value == null || value.isEmpty()) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
