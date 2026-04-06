package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_complete", nullable = false)
    private boolean isComplete = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "srs_enabled", nullable = false)
    private boolean srsEnabled = false;

    // --- SRS Specific Statistics ---

    @Column(name = "repetition_count")
    private int repetitionCount = 0;

    @Column(name = "ease_factor")
    private double easeFactor = 2.5;

    @Column(name = "current_interval")
    private int currentInterval = 0;

    @Column(name = "review_intensity")
    private String reviewIntensity = "STANDARD";

    @Column(name = "next_recall_date")
    private LocalDateTime nextRecallDate;

    // Default constructor required by JPA
    public Task() {
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized constructor
    public Task(Long id, Long userId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSrsEnabled() {
        return srsEnabled;
    }

    public void setSrsEnabled(boolean srsEnabled) {
        this.srsEnabled = srsEnabled;
    }

    public int getRepetitionCount() {
        return repetitionCount;
    }

    public void setRepetitionCount(int repetitionCount) {
        this.repetitionCount = repetitionCount;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(double easeFactor) {
        this.easeFactor = easeFactor;
    }

    public int getCurrentInterval() {
        return currentInterval;
    }

    public void setCurrentInterval(int currentInterval) {
        this.currentInterval = currentInterval;
    }

    public String getReviewIntensity() {
        return reviewIntensity;
    }

    public void setReviewIntensity(String reviewIntensity) {
        this.reviewIntensity = reviewIntensity;
    }

    public LocalDateTime getNextRecallDate() {
        return nextRecallDate;
    }

    public void setNextRecallDate(LocalDateTime nextRecallDate) {
        this.nextRecallDate = nextRecallDate;
    }
}