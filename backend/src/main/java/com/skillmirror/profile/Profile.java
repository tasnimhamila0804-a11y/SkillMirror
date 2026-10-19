package com.skillmirror.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Professional profile — the "Skill Mirror" of the user. */
@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(length = 60)
    private String situation;

    @Column(length = 160)
    private String domain;

    @Column(name = "education_level", length = 120)
    private String educationLevel;

    @Column(name = "experience_summary", length = 2000)
    private String experienceSummary;

    @Column(name = "target_job", length = 160)
    private String targetJob;

    @Column(name = "career_goal", length = 60)
    private String careerGoal;

    @Column(name = "target_company", length = 160)
    private String targetCompany;

    @Column(name = "position_type", length = 40)
    private String positionType;

    @Column(name = "hours_per_week")
    private Integer hoursPerWeek;

    @Column(name = "learning_preference", length = 40)
    private String learningPreference;

    @Column(name = "preferred_difficulty", length = 30)
    private String preferredDifficulty;

    @Column(length = 60)
    private String deadline;

    @Column(length = 220)
    private String headline;

    @Column(length = 4000)
    private String bio;

    @Column(name = "github_url", length = 300)
    private String githubUrl;

    @Column(name = "portfolio_url", length = 300)
    private String portfolioUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public String getExperienceSummary() { return experienceSummary; }
    public void setExperienceSummary(String experienceSummary) { this.experienceSummary = experienceSummary; }
    public String getTargetJob() { return targetJob; }
    public void setTargetJob(String targetJob) { this.targetJob = targetJob; }
    public String getCareerGoal() { return careerGoal; }
    public void setCareerGoal(String careerGoal) { this.careerGoal = careerGoal; }
    public String getTargetCompany() { return targetCompany; }
    public void setTargetCompany(String targetCompany) { this.targetCompany = targetCompany; }
    public String getPositionType() { return positionType; }
    public void setPositionType(String positionType) { this.positionType = positionType; }
    public Integer getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(Integer hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }
    public String getLearningPreference() { return learningPreference; }
    public void setLearningPreference(String learningPreference) { this.learningPreference = learningPreference; }
    public String getPreferredDifficulty() { return preferredDifficulty; }
    public void setPreferredDifficulty(String preferredDifficulty) { this.preferredDifficulty = preferredDifficulty; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void touch() { this.updatedAt = Instant.now(); }
}
