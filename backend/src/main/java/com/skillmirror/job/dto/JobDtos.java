package com.skillmirror.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class JobDtos {

    private JobDtos() {}

    public record JobSkillDto(Long id, Long skillId, String name, String category, String requirement,
                              int weight, String requiredLevel, String source, boolean confirmed) {}

    public record JobOfferDto(Long id, String title, String company, String description,
                              String experienceLevel, String educationRequirement, String extractionStatus,
                              Instant createdAt, Instant updatedAt, List<JobSkillDto> skills) {}

    public record JobSummaryDto(Long id, String title, String company, String extractionStatus,
                                String experienceLevel, int skillCount, Instant createdAt) {}

    public record SaveJobRequest(
            @NotBlank(message = "L'intitulé du poste est requis.") @Size(max = 190) String title,
            @Size(max = 190) String company,
            @NotBlank(message = "Collez le texte de l'offre.") String description,
            String experienceLevel,
            String educationRequirement) {}

    public record UpdateJobRequest(
            @NotBlank(message = "L'intitulé du poste est requis.") @Size(max = 190) String title,
            @Size(max = 190) String company,
            @NotBlank(message = "Collez le texte de l'offre.") String description,
            String experienceLevel,
            String educationRequirement) {}

    /** One reviewed skill of a job offer. */
    public record ReviewedSkill(
            @NotBlank(message = "Le nom de la compétence est requis.") @Size(max = 120) String name,
            String category,
            String requirement,
            Integer weight,
            @Size(max = 30) String requiredLevel) {}

    public record ConfirmSkillsRequest(@Size(max = 40) List<ReviewedSkill> skills) {}

    public record ExtractResponse(JobOfferDto job, boolean simulated) {}
}
