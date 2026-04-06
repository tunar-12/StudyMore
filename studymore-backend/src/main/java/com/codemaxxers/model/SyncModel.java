package com.codemaxxers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class SyncModel {

    // Base User Data (1-to-1 relationships)
    private Map<String, Object> user;
    
    @JsonProperty("user_stats")
    private Map<String, Object> userStats;
    
    private Map<String, Object> settings;
    private Map<String, Object> multipliers;
    private Map<String, Object> inventory;

    // User Data Collections (1-to-Many relationships)
    private List<Map<String, Object>> sessions;
    private List<Map<String, Object>> tasks;
    
    @JsonProperty("user_achievements")
    private List<Map<String, Object>> userAchievements;

    // Nested Relationships
    @JsonProperty("task_srs_history")
    private List<Map<String, Object>> taskSrsHistory;
    
    @JsonProperty("inventory_owned_items")
    private List<Map<String, Object>> inventoryOwnedItems;
    
    @JsonProperty("inventory_equipped_items")
    private List<Map<String, Object>> inventoryEquippedItems;

    // Social & Study Groups
    private List<Map<String, Object>> friends;
    
    @JsonProperty("friend_requests")
    private List<Map<String, Object>> friendRequests;
    
    @JsonProperty("study_groups")
    private List<Map<String, Object>> studyGroups;


    // Getters and Setters

    public Map<String, Object> getUser() { return user; }
    public void setUser(Map<String, Object> user) { this.user = user; }

    public Map<String, Object> getUserStats() { return userStats; }
    public void setUserStats(Map<String, Object> userStats) { this.userStats = userStats; }

    public Map<String, Object> getSettings() { return settings; }
    public void setSettings(Map<String, Object> settings) { this.settings = settings; }

    public Map<String, Object> getMultipliers() { return multipliers; }
    public void setMultipliers(Map<String, Object> multipliers) { this.multipliers = multipliers; }

    public Map<String, Object> getInventory() { return inventory; }
    public void setInventory(Map<String, Object> inventory) { this.inventory = inventory; }

    public List<Map<String, Object>> getSessions() { return sessions; }
    public void setSessions(List<Map<String, Object>> sessions) { this.sessions = sessions; }

    public List<Map<String, Object>> getTasks() { return tasks; }
    public void setTasks(List<Map<String, Object>> tasks) { this.tasks = tasks; }

    public List<Map<String, Object>> getUserAchievements() { return userAchievements; }
    public void setUserAchievements(List<Map<String, Object>> userAchievements) { this.userAchievements = userAchievements; }

    public List<Map<String, Object>> getTaskSrsHistory() { return taskSrsHistory; }
    public void setTaskSrsHistory(List<Map<String, Object>> taskSrsHistory) { this.taskSrsHistory = taskSrsHistory; }

    public List<Map<String, Object>> getInventoryOwnedItems() { return inventoryOwnedItems; }
    public void setInventoryOwnedItems(List<Map<String, Object>> inventoryOwnedItems) { this.inventoryOwnedItems = inventoryOwnedItems; }

    public List<Map<String, Object>> getInventoryEquippedItems() { return inventoryEquippedItems; }
    public void setInventoryEquippedItems(List<Map<String, Object>> inventoryEquippedItems) { this.inventoryEquippedItems = inventoryEquippedItems; }

    public List<Map<String, Object>> getFriends() { return friends; }
    public void setFriends(List<Map<String, Object>> friends) { this.friends = friends; }

    public List<Map<String, Object>> getFriendRequests() { return friendRequests; }
    public void setFriendRequests(List<Map<String, Object>> friendRequests) { this.friendRequests = friendRequests; }

    public List<Map<String, Object>> getStudyGroups() { return studyGroups; }
    public void setStudyGroups(List<Map<String, Object>> studyGroups) { this.studyGroups = studyGroups; }
}