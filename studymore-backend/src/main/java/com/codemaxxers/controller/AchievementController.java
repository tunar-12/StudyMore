package com.codemaxxers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.codemaxxers.model.Achievement;
import com.codemaxxers.model.UserAchievement;
import com.codemaxxers.service.AchievementService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {
 
    private final AchievementService achievementService;
 
    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }
 
    // GET /api/achievements/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserAchievement>> getAllForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(achievementService.getAllForUser(userId));
    }
 
    // GET /api/achievements/user/{userId}/completed
    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<UserAchievement>> getCompleted(@PathVariable Long userId) {
        return ResponseEntity.ok(achievementService.getCompletedForUser(userId));
    }
 
    // GET /api/achievements/user/{userId}/inprogress
    @GetMapping("/user/{userId}/inprogress")
    public ResponseEntity<List<UserAchievement>> getInProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(achievementService.getInProgressForUser(userId));
    }
 
    // POST /api/achievements
    // Body: Achievement JSON
    @PostMapping
    public ResponseEntity<?> createAchievement(@RequestBody Achievement achievement) {
        try {
            return ResponseEntity.ok(achievementService.createAchievement(achievement));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // POST /api/achievements/notify/study-time
    // Body: { "userId": 1, "minutesStudied": 45 }
    @PostMapping("/notify/study-time")
    public ResponseEntity<Void> notifyStudyTime(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        int minutes = Integer.parseInt(body.get("minutesStudied").toString());
        achievementService.notifyStudyTimeAdded(userId, minutes);
        return ResponseEntity.ok().build();
    }
 
    // POST /api/achievements/notify/streak
    // Body: { "userId": 1, "currentStreak": 7 }
    @PostMapping("/notify/streak")
    public ResponseEntity<Void> notifyStreak(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        int streak = Integer.parseInt(body.get("currentStreak").toString());
        achievementService.notifyStreakUpdated(userId, streak);
        return ResponseEntity.ok().build();
    }
 
    // POST /api/achievements/notify/task-completed
    // Body: { "userId": 1 }
    @PostMapping("/notify/task-completed")
    public ResponseEntity<Void> notifyTask(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        achievementService.notifyTaskCompleted(userId);
        return ResponseEntity.ok().build();
    }
 
    // POST /api/achievements/notify/coins-earned
    // Body: { "userId": 1, "coinsEarned": 120 }
    @PostMapping("/notify/coins-earned")
    public ResponseEntity<Void> notifyCoins(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        int coins = Integer.parseInt(body.get("coinsEarned").toString());
        achievementService.notifyCoinsEarned(userId, coins);
        return ResponseEntity.ok().build();
    }
}