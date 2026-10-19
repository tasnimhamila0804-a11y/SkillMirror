package com.skillmirror.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class AdminDtos {

    private AdminDtos() {}

    public record AdminUserDto(Long id, String name, String email, String role, boolean enabled,
                               boolean profileCompleted, Instant createdAt, Instant lastLoginAt,
                               long skillCount, long analysisCount) {}

    public record UserUpdateRequest(Boolean enabled, String role) {}

    public record AdminSkillDto(Long id, String name, String category, boolean custom, Long createdBy) {}

    public record SkillRequest(
            @NotBlank(message = "Le nom est requis.") @Size(max = 120) String name,
            String category) {}

    public record UsageItemDto(Long id, Long userId, String purpose, String mode, String model,
                               int promptTokens, int completionTokens, long latencyMs, String status,
                               String errorMessage, Instant createdAt) {}

    public record UsageAggDto(String purpose, String mode, long calls, long promptTokens,
                              long completionTokens) {}

    public record AiUsageReportDto(long calls, long simulatedCalls, long liveCalls, long errors,
                                   long promptTokens, long completionTokens,
                                   List<UsageAggDto> byPurpose, List<UsageItemDto> recent) {}

    public record ErrorDto(Long id, String level, String message, String path, String method,
                           Long userId, Instant createdAt) {}

    public record SettingInput(String key, String value) {}

    public record OverviewDto(long users, long enabledUsers, long admins, long skillCount,
                              long jobOffers, long analyses, long learningPaths, long validatedPaths,
                              long evidences, long aiCalls, long errors, boolean aiLiveMode) {}
}
