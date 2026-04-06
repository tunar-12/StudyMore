package com.codemaxxers.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Setting {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "dark_mode", nullable = false)
    private boolean darkMode = false;

    @Column(name = "lock_in_mode", nullable = false)
    private boolean lockInMode = false;

    @Column(name = "show_mascot", nullable = false)
    private boolean showMascot = true;

    @Column(name = "study_time", nullable = false)
    private int studyTime = 25;

    @Column(name = "short_break", nullable = false)
    private int shortBreak = 5;

    @Column(name = "long_break", nullable = false)
    private int longBreak = 15;

    @Column(name = "long_break_after", nullable = false)
    private int longBreakAfter = 4;

    @Column(name = "start_sound", nullable = false)
    private boolean startSound = true;

    @Column(name = "break_alert", nullable = false)
    private boolean breakAlert = true;

    @Column(name = "popups", nullable = false)
    private boolean popups = true;

    // Default constructor required by JPA
    public Setting() {
    }

    public Setting(Long id, Long userId) {
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

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isLockInMode() {
        return lockInMode;
    }

    public void setLockInMode(boolean lockInMode) {
        this.lockInMode = lockInMode;
    }

    public boolean isShowMascot() {
        return showMascot;
    }

    public void setShowMascot(boolean showMascot) {
        this.showMascot = showMascot;
    }

    public int getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(int studyTime) {
        this.studyTime = studyTime;
    }

    public int getShortBreak() {
        return shortBreak;
    }

    public void setShortBreak(int shortBreak) {
        this.shortBreak = shortBreak;
    }

    public int getLongBreak() {
        return longBreak;
    }

    public void setLongBreak(int longBreak) {
        this.longBreak = longBreak;
    }

    public int getLongBreakAfter() {
        return longBreakAfter;
    }

    public void setLongBreakAfter(int longBreakAfter) {
        this.longBreakAfter = longBreakAfter;
    }

    public boolean isStartSound() {
        return startSound;
    }

    public void setStartSound(boolean startSound) {
        this.startSound = startSound;
    }

    public boolean isBreakAlert() {
        return breakAlert;
    }

    public void setBreakAlert(boolean breakAlert) {
        this.breakAlert = breakAlert;
    }

    public boolean isPopups() {
        return popups;
    }

    public void setPopups(boolean popups) {
        this.popups = popups;
    }
}