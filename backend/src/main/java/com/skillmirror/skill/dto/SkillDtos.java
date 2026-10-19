package com.skillmirror.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class SkillDtos {

    private SkillDtos() {}

    public record SkillDto(Long id, String name, String category, boolean custom) {}

    public record UserSkillDto(Long id, Long skillId, String name, String category, String level,
                               String status, String notes, Instant updatedAt) {}

    public record AddSkillRequest(
            Long skillId,
            @Size(max = 120) String name,
            String category,
            String level,
            @Size(max = 1000) String notes) {}

    public record UpdateSkillRequest(String level, @Size(max = 1000) String notes) {}
}
