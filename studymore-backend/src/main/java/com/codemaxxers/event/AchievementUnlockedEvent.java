package com.codemaxxers.event;

import org.springframework.context.ApplicationEvent;

import com.codemaxxers.model.UserAchievement;

public class AchievementUnlockedEvent extends ApplicationEvent {
 
    private final UserAchievement userAchievement;
 
    public AchievementUnlockedEvent(Object source, UserAchievement userAchievement) {
        super(source);
        this.userAchievement = userAchievement;
    }
 
    public UserAchievement getUserAchievement() {
        return userAchievement;
    }
 
    public Long getUserId() {
        return userAchievement.getUser().getUserId();
    }
 
    public String getAchievementTitle() {
        return userAchievement.getAchievement().getTitle();
    }
 
    public int getCoinReward() {
        return userAchievement.getAchievement().getCoinReward();
    }
}