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
            syncRepository.upsertMultipleRows("user_achievements", "id", payload.getUserAchievements());
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
            syncRepository.upsertMultipleRowsComposite("friends", 
                java.util.List.of("user_id", "friend_id"), payload.getFriends());
        }

        if (payload.getFriendRequests() != null) {
            syncRepository.upsertMultipleRows("friend_requests", "id", payload.getFriendRequests());
        }
        if (payload.getStudyGroups() != null) {
            syncRepository.upsertMultipleRows("study_groups", "id", payload.getStudyGroups());
        }
    }
}