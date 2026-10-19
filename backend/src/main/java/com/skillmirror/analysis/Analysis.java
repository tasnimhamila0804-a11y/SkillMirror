package com.skillmirror.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** One saved compatibility analysis between a profile and a job offer. */
@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_offer_id", nullable = false)
    private Long jobOfferId;

    @Column(nullable = false)
    private int score = 0;

    @Column(length = 2000)
    private String summary;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(name = "simulated_ai", nullable = false)
    private boolean simulatedAi = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public boolean isSimulatedAi() { return simulatedAi; }
    public void setSimulatedAi(boolean simulatedAi) { this.simulatedAi = simulatedAi; }
    public Instant getCreatedAt() { return createdAt; }
}
