package com.codemaxxers.service;

import com.codemaxxers.model.SyncModel;
import com.codemaxxers.repository.SyncRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncService {

    private final SyncRepository syncRepository;

    @Autowired
    public SyncService(SyncRepository syncRepository) {
        this.syncRepository = syncRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processSync(SyncModel payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Sync payload cannot be null");
        }

        if (payload.getUser() != null && !payload.getUser().isEmpty()) {
            java.util.Map<String, Object> combinedUser = new java.util.HashMap<>(payload.getUser());
            
            if (payload.getUserStats() != null && !payload.getUserStats().isEmpty()) {
                combinedUser.putAll(payload.getUserStats());
                combinedUser.remove("user_id"); 
            }

            combinedUser.putIfAbsent("coin_balance", 0);
            combinedUser.putIfAbsent("rating", 0);
            combinedUser.putIfAbsent("study_streak", 0);
            combinedUser.putIfAbsent("total_study_time", 0L);
            combinedUser.putIfAbsent("daily_study_time", 0L);
            combinedUser.putIfAbsent("rank", "BRONZE");
            
            syncRepository.upsertSingleRow("users", "id", combinedUser);
        }

        // 1 to 1
        if (payload.getUserStats() != null && !payload.getUserStats().isEmpty()) {
            syncRepository.upsertSingleRow("user_stats", "id", payload.getUserStats());
        }
        if (payload.getSettings() != null && !payload.getSettings().isEmpty()) {
            syncRepository.upsertSingleRow("settings", "id", payload.getSettings());
        }
        if (payload.getMultipliers() != null && !payload.getMultipliers().isEmpty()) {
            syncRepository.upsertSingleRow("multipliers", "id", payload.getMultipliers());
        }
        if (payload.getInventory() != null && !payload.getInventory().isEmpty()) {
            syncRepository.upsertSingleRow("inventory", "id", payload.getInventory());
        }

        // 1-to-Many Collections
        if (payload.getTasks() != null) {
            syncRepository.upsertMultipleRows("tasks", "id", payload.getTasks());
        }
        if (payload.getSessions() != null) {
            syncRepository.upsertMultipleRows("sessions", "id", payload.getSessions());
        }
        if (payload.getUserAchievements() != null) {
            syncRepository.upsertMultipleRowsComposite("user_achievements", 
                java.util.List.of("user_id", "achievement_id"), payload.getUserAchievements());
        }

        // nested
        if (payload.getTaskSrsHistory() != null) {
            syncRepository.upsertMultipleRows("task_srs_history", "id", payload.getTaskSrsHistory());
        }
        if (payload.getInventoryOwnedItems() != null) {
  
            syncRepository.upsertMultipleRowsComposite("inventory_owned_items", 
                java.util.List.of("inventory_id", "cosmetic_id"), payload.getInventoryOwnedItems());
        }

        if (payload.getInventoryEquippedItems() != null) {

            syncRepository.upsertMultipleRowsComposite("inventory_equipped_items", 
                java.util.List.of("inventory_id", "cosmetic_type"), payload.getInventoryEquippedItems());
        }

        // Social & Study Groups
        if (payload.getFriends() != null) {
            syncRepository.upsertMultipleRowsComposite("user_friends",
                java.util.List.of("user_id", "friend_id"), payload.getFriends());
        }

        if (payload.getFriendRequests() != null) {
            syncRepository.upsertMultipleRows("friend_requests", "id", payload.getFriendRequests());
        }
        if (payload.getStudyGroups() != null) {
            syncRepository.upsertMultipleRows("study_groups", "id", payload.getStudyGroups());
        }
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public SyncModel exportUserData(Long userId) {
        SyncModel payload = new SyncModel();

        // 1-to-1 Mappings
        var users = jdbcTemplate.queryForList("SELECT * FROM users WHERE id = ?", userId);
        if (!users.isEmpty()) {
            java.util.Map<String, Object> backendUser = users.get(0);
            
            // Build strictly frontend-compatible user object
            java.util.Map<String, Object> frontendUser = new java.util.HashMap<>();
            frontendUser.put("id", backendUser.get("id"));
            frontendUser.put("username", backendUser.get("username"));
            frontendUser.put("email", backendUser.get("email"));
            frontendUser.put("password_hash", backendUser.get("password_hash"));
            frontendUser.put("created_at", backendUser.get("created_at"));
            
            // Build strictly frontend-compatible user_stats object
            java.util.Map<String, Object> frontendStats = new java.util.HashMap<>();
            frontendStats.put("user_id", backendUser.get("id")); 
            frontendStats.put("rank", backendUser.get("rank"));
            frontendStats.put("rating", backendUser.get("rating"));
            frontendStats.put("coin_balance", backendUser.get("coin_balance"));
            frontendStats.put("study_streak", backendUser.get("study_streak"));
            frontendStats.put("total_study_time", backendUser.get("total_study_time"));
            frontendStats.put("daily_study_time", backendUser.get("daily_study_time"));
            
            payload.setUser(frontendUser);
            payload.setUserStats(frontendStats);
        }

        var settings = jdbcTemplate.queryForList("SELECT * FROM settings WHERE id = ?", userId);
        if (!settings.isEmpty()) payload.setSettings(settings.get(0));

        var multipliers = jdbcTemplate.queryForList("SELECT * FROM multipliers WHERE id = ?", userId);
        if (!multipliers.isEmpty()) payload.setMultipliers(multipliers.get(0));

        var inventory = jdbcTemplate.queryForList("SELECT * FROM inventory WHERE id = ?", userId);
        if (!inventory.isEmpty()) payload.setInventory(inventory.get(0));

        // 1-to-Many Collections
        payload.setTasks(jdbcTemplate.queryForList("SELECT * FROM tasks WHERE user_id = ?", userId));
        payload.setSessions(jdbcTemplate.queryForList("SELECT * FROM sessions WHERE user_id = ?", userId));
        payload.setUserAchievements(jdbcTemplate.queryForList("SELECT * FROM user_achievements WHERE user_id = ?", userId));

        // Nested / Join Tables
        payload.setTaskSrsHistory(jdbcTemplate.queryForList(
            "SELECT tsh.* FROM task_srs_history tsh JOIN tasks t ON tsh.task_id = t.id WHERE t.user_id = ?", userId));
            
        payload.setInventoryOwnedItems(jdbcTemplate.queryForList(
            "SELECT ioi.* FROM inventory_owned_items ioi JOIN inventory i ON ioi.inventory_id = i.id WHERE i.user_id = ?", userId));
            
        payload.setInventoryEquippedItems(jdbcTemplate.queryForList(
            "SELECT iei.* FROM inventory_equipped_items iei JOIN inventory i ON iei.inventory_id = i.id WHERE i.user_id = ?", userId));

        // Social
        payload.setFriends(jdbcTemplate.queryForList(
            "SELECT * FROM user_friends WHERE user_id = ? OR friend_id = ?", 
            userId, userId
        ));
        payload.setFriendRequests(jdbcTemplate.queryForList("SELECT * FROM friend_requests WHERE sender_id = ? OR receiver_id = ?", userId, userId));
        payload.setStudyGroups(jdbcTemplate.queryForList(
            "SELECT DISTINCT sg.* FROM study_groups sg LEFT JOIN study_group_members sgm ON sg.id = sgm.group_id WHERE sg.host_id = ? OR sgm.user_id = ?", userId, userId));

        return payload;
    }
}