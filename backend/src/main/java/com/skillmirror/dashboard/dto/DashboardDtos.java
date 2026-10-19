package com.skillmirror.dashboard.dto;

import com.skillmirror.analysis.dto.AnalysisDtos.AnalysisSummaryDto;
import com.skillmirror.learning.dto.LearningDtos.EvidenceDto;
import com.skillmirror.learning.dto.LearningDtos.LearningPathSummaryDto;
import java.time.Instant;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {}

    public record PrioritySkillDto(String skillName, String matchCategory, int priority, String note) {}

    public record StatsDto(long activePaths, long completedPaths, long totalTasks, long doneTasks,
                           int progressRate, long totalSkills, long demonstratedSkills,
                           long evaluatedSkills, long declaredSkills, long analysesCount, long offersCount) {}

    public record RecommendedActionDto(String title, String description, String url, String priority) {}

    public record DashboardDto(
            String firstName,
            boolean profileCompleted,
            Integer lastScore,
            String lastScoreJob,
            Instant lastAnalysisDate,
            List<PrioritySkillDto> prioritySkills,
            StatsDto stats,
            List<AnalysisSummaryDto> recentAnalyses,
            List<RecommendedActionDto> recommendedActions,
            boolean emptyState) {}

    public record ScorePointDto(Long analysisId, int score, String jobTitle, Instant date) {}

    public record SkillProgressDto(Long userSkillId, String skillName, String category, String level,
                                   String status, Instant updatedAt) {}

    public record TimelineEventDto(String type, String label, String detail, Instant date) {}

    public record ProgressDto(
            List<LearningPathSummaryDto> paths,
            List<ScorePointDto> scoreHistory,
            List<SkillProgressDto> skillProgress,
            List<EvidenceDto> evidences,
            List<TimelineEventDto> timeline,
            long totalTasks,
            long doneTasks,
            long verifiedProofs) {}
}
