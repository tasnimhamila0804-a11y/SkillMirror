package com.skillmirror.profile.dto;

import com.skillmirror.profile.Certificate;
import com.skillmirror.profile.Experience;
import com.skillmirror.profile.Profile;
import com.skillmirror.profile.UserProject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ProfileDtos {

    private ProfileDtos() {}

    public record ProfileDto(
            Long id,
            Long userId,
            String situation,
            String domain,
            String educationLevel,
            String experienceSummary,
            String targetJob,
            String careerGoal,
            String targetCompany,
            String positionType,
            Integer hoursPerWeek,
            String learningPreference,
            String preferredDifficulty,
            String deadline,
            String headline,
            String bio,
            String githubUrl,
            String portfolioUrl,
            Instant updatedAt) {

        public static ProfileDto from(Profile p) {
            return new ProfileDto(p.getId(), p.getUserId(), p.getSituation(), p.getDomain(),
                    p.getEducationLevel(), p.getExperienceSummary(), p.getTargetJob(), p.getCareerGoal(),
                    p.getTargetCompany(), p.getPositionType(), p.getHoursPerWeek(),
                    p.getLearningPreference(), p.getPreferredDifficulty(), p.getDeadline(),
                    p.getHeadline(), p.getBio(), p.getGithubUrl(), p.getPortfolioUrl(), p.getUpdatedAt());
        }
    }

    /** Patch semantics: null leaves the field unchanged, an empty string clears it. */
    public record UpdateProfileRequest(String situation, String domain, String educationLevel,
                                       String experienceSummary, String targetJob, String careerGoal,
                                       String targetCompany, String positionType, Integer hoursPerWeek,
                                       String learningPreference, String preferredDifficulty, String deadline,
                                       String headline, String bio, String githubUrl, String portfolioUrl) {}

    public record ExperienceDto(Long id, String title, String organization, String type, String description,
                                String startDate, String endDate, boolean current) {
        public static ExperienceDto from(Experience e) {
            return new ExperienceDto(e.getId(), e.getTitle(), e.getOrganization(), e.getType(),
                    e.getDescription(), e.getStartDate(), e.getEndDate(), e.isCurrent());
        }
    }

    public record ExperienceRequest(
            @NotBlank(message = "Le titre est requis.") @Size(max = 190) String title,
            @Size(max = 190) String organization,
            String type,
            @Size(max = 4000) String description,
            String startDate, String endDate, Boolean current) {}

    public record CertificateDto(Long id, String name, String issuer, String issuedDate, String url) {
        public static CertificateDto from(Certificate c) {
            return new CertificateDto(c.getId(), c.getName(), c.getIssuer(), c.getIssuedDate(), c.getUrl());
        }
    }

    public record CertificateRequest(
            @NotBlank(message = "Le nom de la certification est requis.") @Size(max = 190) String name,
            @Size(max = 190) String issuer, String issuedDate,
            @Size(max = 400) String url) {}

    public record ProjectDto(Long id, String name, String description, String repositoryUrl, String demoUrl,
                             String technologies, String startedDate, String endedDate) {
        public static ProjectDto from(UserProject p) {
            return new ProjectDto(p.getId(), p.getName(), p.getDescription(), p.getRepositoryUrl(),
                    p.getDemoUrl(), p.getTechnologies(), p.getStartedDate(), p.getEndedDate());
        }
    }

    public record ProjectRequest(
            @NotBlank(message = "Le nom du projet est requis.") @Size(max = 190) String name,
            @Size(max = 4000) String description,
            @Size(max = 400) String repositoryUrl,
            @Size(max = 400) String demoUrl,
            @Size(max = 600) String technologies,
            String startedDate, String endedDate) {}
}
