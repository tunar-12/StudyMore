package com.codemaxxers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "multipliers")
public class Multiplier {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "current_value", nullable = false)
    private double currentValue = 1.0;

    @Column(name = "max_value", nullable = false)
    private double maxValue = 5.0;

    @Column(name = "increment_interval", nullable = false)
    private int incrementInterval = 0;

    @Column(name = "cooldown_interval", nullable = false)
    private int cooldownInterval = 0;

    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;

    // Default constructor required by JPA
    public Multiplier() {
    }

    public Multiplier(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
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

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public int getIncrementInterval() {
        return incrementInterval;
    }

    public void setIncrementInterval(int incrementInterval) {
        this.incrementInterval = incrementInterval;
    }

    public int getCooldownInterval() {
        return cooldownInterval;
    }

    public void setCooldownInterval(int cooldownInterval) {
        this.cooldownInterval = cooldownInterval;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
}