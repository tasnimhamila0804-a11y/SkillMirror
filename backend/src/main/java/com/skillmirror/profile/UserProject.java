package com.skillmirror.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A project realised by the user (portfolio), as opposed to a generated learning path. */
@Entity
@Table(name = "projects")
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 190)
    private String name;

    @Column(length = 4000)
    private String description;

    @Column(name = "repository_url", length = 400)
    private String repositoryUrl;

    @Column(name = "demo_url", length = 400)
    private String demoUrl;

    @Column(length = 600)
    private String technologies;

    @Column(name = "started_date", length = 30)
    private String startedDate;

    @Column(name = "ended_date", length = 30)
    private String endedDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }
    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }
    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }
    public String getStartedDate() { return startedDate; }
    public void setStartedDate(String startedDate) { this.startedDate = startedDate; }
    public String getEndedDate() { return endedDate; }
    public void setEndedDate(String endedDate) { this.endedDate = endedDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void touch() { this.updatedAt = Instant.now(); }
}
