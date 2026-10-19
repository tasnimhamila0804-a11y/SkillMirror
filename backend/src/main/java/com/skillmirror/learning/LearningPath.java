package com.skillmirror.learning;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A generated practice project / learning path targeting one missing skill. */
@Entity
@Table(name = "learning_paths")
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "analysis_id")
    private Long analysisId;

    @Column(name = "analysis_detail_id")
    private Long analysisDetailId;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "skill_name", nullable = false, length = 120)
    private String skillName;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    /** BEGINNER | INTERMEDIATE | ADVANCED (stored with a human label) */
    @Column(nullable = false, length = 30)
    private String difficulty = "INTERMEDIATE";

    @Column(columnDefinition = "text")
    private String objectives;

    @Column(columnDefinition = "text")
    private String tools;

    @Column(columnDefinition = "text")
    private String steps;

    @Column(name = "evaluation_criteria", columnDefinition = "text")
    private String evaluationCriteria;

    @Column(name = "expected_result", length = 2000)
    private String expectedResult;

    @Column(name = "proof_required", length = 1000)
    private String proofRequired;

    /** NOT_STARTED | IN_PROGRESS | PAUSED | COMPLETED | VALIDATED */
    @Column(nullable = false, length = 30)
    private String status = "NOT_STARTED";

    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated = false;

    @Column(name = "simulated_ai", nullable = false)
    private boolean simulatedAi = false;

    @Column(name = "validation_note", length = 2000)
    private String validationNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getAnalysisId() { return analysisId; }
    public void setAnalysisId(Long analysisId) { this.analysisId = analysisId; }
    public Long getAnalysisDetailId() { return analysisDetailId; }
    public void setAnalysisDetailId(Long analysisDetailId) { this.analysisDetailId = analysisDetailId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }
    public String getTools() { return tools; }
    public void setTools(String tools) { this.tools = tools; }
    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }
    public String getEvaluationCriteria() { return evaluationCriteria; }
    public void setEvaluationCriteria(String evaluationCriteria) { this.evaluationCriteria = evaluationCriteria; }
    public String getExpectedResult() { return expectedResult; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }
    public String getProofRequired() { return proofRequired; }
    public void setProofRequired(String proofRequired) { this.proofRequired = proofRequired; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
    public boolean isSimulatedAi() { return simulatedAi; }
    public void setSimulatedAi(boolean simulatedAi) { this.simulatedAi = simulatedAi; }
    public String getValidationNote() { return validationNote; }
    public void setValidationNote(String validationNote) { this.validationNote = validationNote; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void touch() { this.updatedAt = Instant.now(); }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
