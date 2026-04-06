package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_srs_history")
public class TaskSrsHistory {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "ease_factor_at_time")
    private Double easeFactorAtTime;

    @Column(name = "interval_at_time")
    private Integer intervalAtTime;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // Default constructor required by JPA
    public TaskSrsHistory() {
        this.timestamp = LocalDateTime.now();
    }

    public TaskSrsHistory(Long id, Long taskId, Double easeFactorAtTime, Integer intervalAtTime, Integer qualityScore) {
        this.id = id;
        this.taskId = taskId;
        this.easeFactorAtTime = easeFactorAtTime;
        this.intervalAtTime = intervalAtTime;
        this.qualityScore = qualityScore;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Double getEaseFactorAtTime() {
        return easeFactorAtTime;
    }

    public void setEaseFactorAtTime(Double easeFactorAtTime) {
        this.easeFactorAtTime = easeFactorAtTime;
    }

    public Integer getIntervalAtTime() {
        return intervalAtTime;
    }

    public void setIntervalAtTime(Integer intervalAtTime) {
        this.intervalAtTime = intervalAtTime;
    }

    public Integer getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}