package com.codemaxxers.event;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
@Component
public class AchievementUnlockedListener {
 
    private final SimpMessagingTemplate messagingTemplate;
 
    public AchievementUnlockedListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
 
    @Async
    @EventListener
    public void onAchievementUnlocked(AchievementUnlockedEvent event) {
        String destination = "/user/" + event.getUserId() + "/queue/achievements";

        String payload = "{" +
            "\"type\": \"ACHIEVEMENT_UNLOCKED\"," +
            "\"achievementTitle\": \"" + event.getAchievementTitle() + "\"," +
            "\"coinReward\": " + event.getCoinReward() + "," +
            "\"userId\": " + event.getUserId() +
        "}";

        messagingTemplate.convertAndSend(destination, payload);
    }
}