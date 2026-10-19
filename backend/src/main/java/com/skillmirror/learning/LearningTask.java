package com.skillmirror.learning;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "learning_tasks")
public class LearningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learning_path_id", nullable = false)
    private Long learningPathId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int position = 0;

    @Column(nullable = false)
    private boolean done = false;

    @Column(length = 2000)
    private String note;

    @Column(name = "done_at")
    private Instant doneAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getLearningPathId() { return learningPathId; }
    public void setLearningPathId(Long learningPathId) { this.learningPathId = learningPathId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getDoneAt() { return doneAt; }
    public void setDoneAt(Instant doneAt) { this.doneAt = doneAt; }
    public Instant getCreatedAt() { return createdAt; }
}
