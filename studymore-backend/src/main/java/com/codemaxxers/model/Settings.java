package com.codemaxxers.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private boolean darkMode;
    private boolean lockInMode;
    private boolean mascotVisible;

    private int studyTime;
    private int shortBreak;
    private int longBreak;
    private int longBreakAfter;

    private boolean startSound;
    private boolean breakAlert;
    private boolean popups;

    public Settings() {}

    public Settings(User user) {
        this.user = user;
        this.darkMode = false;
        this.lockInMode = false;
        this.mascotVisible = true;
        this.studyTime = 25;
        this.shortBreak = 5;
        this.longBreak = 15;
        this.longBreakAfter = 4;
        this.startSound = true;
        this.breakAlert = true;
        this.popups = true;
    }

    public Long getId() { 
        return id; 
    }
    public User getUser() { 
        return user; 
    }
    public boolean isDarkMode() { 
        return darkMode; 
    }
    public boolean isLockInMode() { 
        return lockInMode; 
    }
    public boolean isMascotVisible() { 
        return mascotVisible; 
    }
    public int getStudyTime() { 
        return studyTime; 
    }
    public int getShortBreak() { 
        return shortBreak; 
    }
    public int getLongBreak() { 
        return longBreak; 
    }
    public int getLongBreakAfter() { 
        return longBreakAfter; 
    }
    public boolean isStartSound() { 
        return startSound; 
    }
    public boolean isBreakAlert() { 
        return breakAlert; 
    }
    public boolean isPopups() { 
        return popups; 
    }


    
    public void setUser(User user) { 
        this.user = user; 
    }
    public void setDarkMode(boolean darkMode) { 
        this.darkMode = darkMode; 
    }
    public void setLockInMode(boolean lockInMode) { 
        this.lockInMode = lockInMode; 
    }
    public void setMascotVisible(boolean mascotVisible) { 
        this.mascotVisible = mascotVisible; 
    }
    public void setStudyTime(int studyTime) { 
        this.studyTime = studyTime; 
    }
    public void setShortBreak(int shortBreak) { 
        this.shortBreak = shortBreak; 
    }
    public void setLongBreak(int longBreak) { 
        this.longBreak = longBreak; 
    }
    public void setLongBreakAfter(int longBreakAfter) { 
        this.longBreakAfter = longBreakAfter; 
    }
    public void setStartSound(boolean startSound) { 
        this.startSound = startSound; 
    }
    public void setBreakAlert(boolean breakAlert) { 
        this.breakAlert = breakAlert; 
    }
    public void setPopups(boolean popups) { 
        this.popups = popups; 
    }
}