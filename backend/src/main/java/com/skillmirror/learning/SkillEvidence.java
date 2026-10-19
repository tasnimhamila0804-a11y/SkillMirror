package com.skillmirror.learning;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A proof of skill: repository link, uploaded file or written report. */
@Entity
@Table(name = "skill_evidence")
public class SkillEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "skill_name", nullable = false, length = 120)
    private String skillName;

    @Column(name = "learning_path_id")
    private Long learningPathId;

    @Column(name = "project_id")
    private Long projectId;

    /** LINK | FILE | REPORT */
    @Column(nullable = false, length = 20)
    private String type = "LINK";

    @Column(length = 600)
    private String url;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 600)
    private String filePath;

    @Column(length = 4000)
    private String description;

    /** SUBMITTED | VERIFIED | REJECTED */
    @Column(nullable = false, length = 20)
    private String status = "SUBMITTED";

    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public Long getLearningPathId() { return learningPathId; }
    public void setLearningPathId(Long learningPathId) { this.learningPathId = learningPathId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public Instant getCreatedAt() { return createdAt; }
}
