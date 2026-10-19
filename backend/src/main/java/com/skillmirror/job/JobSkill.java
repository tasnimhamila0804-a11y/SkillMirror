package com.skillmirror.job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "job_skills")
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_offer_id", nullable = false)
    private Long jobOfferId;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "skill_name", nullable = false, length = 120)
    private String skillName;

    @Column(name = "normalized_name", nullable = false, length = 140)
    private String normalizedName;

    @Column(nullable = false, length = 40)
    private String category = "OTHER";

    /** REQUIRED | DESIRED */
    @Column(nullable = false, length = 20)
    private String requirement = "REQUIRED";

    @Column(nullable = false)
    private int weight = 3;

    @Column(name = "required_level", length = 30)
    private String requiredLevel;

    /** AI | MANUAL */
    @Column(nullable = false, length = 20)
    private String source = "AI";

    @Column(nullable = false)
    private boolean confirmed = false;

    public Long getId() { return id; }
    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public String getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(String requiredLevel) { this.requiredLevel = requiredLevel; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
}
