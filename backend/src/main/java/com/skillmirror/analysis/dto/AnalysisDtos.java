package com.skillmirror.analysis.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class AnalysisDtos {

    private AnalysisDtos() {}

    public record AnalysisSummaryDto(Long id, Long jobOfferId, String jobTitle, String company,
                                     int score, String summary, boolean simulatedAi, Instant createdAt) {}

    public record Counts(int demonstrated, int declared, int partial, int missing, int total) {}

    public record AnalysisDetailDto(Long id, String skillName, String category, String matchCategory,
                                    String requiredLevel, String userLevel, int weight,
                                    BigDecimal masteryScore, int priority, String note, Long userSkillId) {}

    public record AnalysisResultDto(AnalysisSummaryDto analysis, String explanation, Counts counts,
                                    List<AnalysisDetailDto> details, boolean simulatedAi) {}

    public record GapDto(String skillName, String matchCategory, int priority, String note) {}
}
