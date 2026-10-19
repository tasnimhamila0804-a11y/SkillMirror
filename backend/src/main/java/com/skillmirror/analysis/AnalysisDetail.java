package com.skillmirror.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Per-skill detail of an analysis: match category, mastery, weight, priority. */
@Entity
@Table(name = "analysis_details")
public class AnalysisDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Column(name = "user_skill_id")
    private Long userSkillId;

    @Column(name = "skill_name", nullable = false, length = 120)
    private String skillName;

    @Column(name = "normalized_name", nullable = false, length = 140)
    private String normalizedName;

    @Column(nullable = false, length = 40)
    private String category = "OTHER";

    /** DEMONSTRATED | DECLARED | PARTIAL | MISSING */
    @Column(name = "match_category", nullable = false, length = 40)
    private String matchCategory;

    @Column(name = "required_level", length = 30)
    private String requiredLevel;

    @Column(name = "user_level", length = 30)
    private String userLevel;

    @Column(nullable = false)
    private int weight = 3;

    @Column(name = "mastery_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal masteryScore = BigDecimal.ZERO;

    @Column(nullable = false)
    private int priority = 0;

    @Column(length = 1000)
    private String note;

    public Long getId() { return id; }
    public Long getAnalysisId() { return analysisId; }
    public void setAnalysisId(Long analysisId) { this.analysisId = analysisId; }
    public Long getUserSkillId() { return userSkillId; }
    public void setUserSkillId(Long userSkillId) { this.userSkillId = userSkillId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMatchCategory() { return matchCategory; }
    public void setMatchCategory(String matchCategory) { this.matchCategory = matchCategory; }
    public String getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(String requiredLevel) { this.requiredLevel = requiredLevel; }
    public String getUserLevel() { return userLevel; }
    public void setUserLevel(String userLevel) { this.userLevel = userLevel; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public java.math.BigDecimal getMasteryScore() { return masteryScore; }
    public void setMasteryScore(java.math.BigDecimal masteryScore) { this.masteryScore = masteryScore; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
