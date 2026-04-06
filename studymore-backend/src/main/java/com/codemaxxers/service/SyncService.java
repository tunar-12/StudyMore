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

        // Base User Data (Parent Tables)
        if (payload.getUser() != null && !payload.getUser().isEmpty()) {
            syncRepository.upsertSingleRow("users", "id", payload.getUser());
        }
        
        if (payload.getUserStats() != null && !payload.getUserStats().isEmpty()) {
            syncRepository.upsertSingleRow("user_stats", "user_id", payload.getUserStats());
        }
        
        if (payload.getSettings() != null && !payload.getSettings().isEmpty()) {
            syncRepository.upsertSingleRow("settings", "user_id", payload.getSettings());
        }
        
        if (payload.getMultipliers() != null && !payload.getMultipliers().isEmpty()) {
            syncRepository.upsertSingleRow("multipliers", "user_id", payload.getMultipliers());
        }
        
        if (payload.getInventory() != null && !payload.getInventory().isEmpty()) {
            syncRepository.upsertSingleRow("inventory", "user_id", payload.getInventory());
        }

        // Collections (Child Tables)
        if (payload.getSessions() != null) {
            syncRepository.upsertMultipleRows("sessions", "id", payload.getSessions());
        }
        
        if (payload.getTasks() != null) {
            syncRepository.upsertMultipleRows("tasks", "id", payload.getTasks());
        }
        
        if (payload.getUserAchievements() != null) {
            syncRepository.upsertMultipleRows("user_achievements", "id", payload.getUserAchievements());
        }


        // Nested Relationships (Grandchild Tables)
        if (payload.getTaskSrsHistory() != null) {
            syncRepository.upsertMultipleRows("task_srs_history", "id", payload.getTaskSrsHistory());
        }
        
        if (payload.getInventoryOwnedItems() != null) {
            syncRepository.upsertMultipleRows("inventory_owned_items", "id", payload.getInventoryOwnedItems());
        }
        
        if (payload.getInventoryEquippedItems() != null) {
            syncRepository.upsertMultipleRows("inventory_equipped_items", "id", payload.getInventoryEquippedItems());
        }

        // Social Data
        if (payload.getFriends() != null) {
            syncRepository.upsertMultipleRows("friends", "id", payload.getFriends());
        }
        
        if (payload.getFriendRequests() != null) {
            syncRepository.upsertMultipleRows("friend_requests", "id", payload.getFriendRequests());
        }
        
        if (payload.getStudyGroups() != null) {
            syncRepository.upsertMultipleRows("study_groups", "id", payload.getStudyGroups());
        }
    }
}