package com.codemaxxers.model;

import jakarta.persistence.*;

@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "target_value", nullable = false)
    private int targetValue = 0;

    @Column(name = "reward", nullable = false)
    private int reward = 0;

    @Column(name = "icon_path", columnDefinition = "TEXT")
    private String iconPath;

    // Default constructor required by JPA
    public Achievement() {
    }

    public Achievement(Long id, String title, String type, int targetValue, int reward) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.targetValue = targetValue;
        this.reward = reward;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}