package com.skillmirror.ai.dto;

import java.util.List;

/** Structured AI results. Every payload carries a `simulated` flag: simulated results are always shown as such. */
public final class AiDtos {

    private AiDtos() {}

    public record ExtractedSkill(
            String name,
            String category,
            /** REQUIRED | DESIRED */
            String requirement,
            /** 3 = critical, 2 = important, 1 = nice to have */
            int weight,
            String requiredLevel) {}

    public record ExtractionResult(
            List<ExtractedSkill> technical,
            List<ExtractedSkill> softSkills,
            String experienceLevel,
            String educationRequirement,
            boolean simulated) {}

    public record GeneratedProject(
            String title,
            String description,
            String difficulty,
            List<String> objectives,
            List<String> tools,
            List<String> steps,
            List<String> tasks,
            List<String> evaluationCriteria,
            String expectedResult,
            String proofRequired,
            boolean simulated) {}

    public record AnalysisText(String summary, String explanation, boolean simulated) {}
}
