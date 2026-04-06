package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "multiplier_value", nullable = false)
    private double multiplierValue = 1.0;

    @Column(name = "coins_earned", nullable = false)
    private int coinsEarned = 0;

    @Column(name = "duration", nullable = false)
    private int duration = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor required by JPA
    public Session() {
        this.createdAt = LocalDateTime.now();
    }

    public Session(Long id, Long userId, LocalDateTime startTime) {
        this.id = id;
        this.userId = userId;
        this.startTime = startTime;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getMultiplierValue() {
        return multiplierValue;
    }

    public void setMultiplierValue(double multiplierValue) {
        this.multiplierValue = multiplierValue;
    }

    public int getCoinsEarned() {
        return coinsEarned;
    }

    public void setCoinsEarned(int coinsEarned) {
        this.coinsEarned = coinsEarned;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}