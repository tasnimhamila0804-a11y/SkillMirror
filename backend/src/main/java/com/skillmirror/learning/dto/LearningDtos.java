package com.skillmirror.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class LearningDtos {

    private LearningDtos() {}

    public record TaskDto(Long id, String title, String description, int position, boolean done,
                          String note, Instant doneAt) {}

    public record EvidenceDto(Long id, Long skillId, String skillName, Long learningPathId, Long projectId,
                              String type, String url, String fileName, String description, String status,
                              String reviewNote, Instant createdAt) {}

    public record LearningPathDto(Long id, Long analysisId, Long analysisDetailId, String skillName,
                                  String title, String description, String difficulty,
                                  List<String> objectives, List<String> tools, List<String> steps,
                                  List<String> evaluationCriteria, String expectedResult, String proofRequired,
                                  String status, boolean aiGenerated, boolean simulatedAi,
                                  String validationNote, Instant createdAt, Instant completedAt,
                                  int taskCount, int doneCount, int progressPercent,
                                  List<TaskDto> tasks, List<EvidenceDto> evidence) {}

    public record LearningPathSummaryDto(Long id, String skillName, String title, String difficulty,
                                         String status, int taskCount, int doneCount, int progressPercent,
                                         boolean simulatedAi, Instant createdAt, Instant completedAt) {}

    public record GenerateRequest(Long analysisId, Integer limit) {}

    public record TaskUpdateRequest(Boolean done, @Size(max = 2000) String note) {}

    public record PathStatusRequest(@NotBlank String status) {}

    public record SubmitEvidenceRequest(
            String type,
            @Size(max = 600) String url,
            @Size(max = 4000) String description,
            Long projectId) {}

    public record CompletionCheckDto(boolean canComplete, List<String> missing, int doneTasks, int totalTasks,
                                     boolean hasProof, boolean criteriaDefined) {}
}
