package com.codemaxxers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.codemaxxers.model.StudyGroup;
import com.codemaxxers.model.User;
import com.codemaxxers.service.StudyGroupService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class StudyGroupController {
 
    private final StudyGroupService studyGroupService;
 
    public StudyGroupController(StudyGroupService studyGroupService) {
        this.studyGroupService = studyGroupService;
    }
 
    // GET /api/groups/active
    @GetMapping("/active")
    public ResponseEntity<List<StudyGroup>> getActiveGroups() {
        return ResponseEntity.ok(studyGroupService.getActiveGroups());
    }
 
    // GET /api/groups/search?keyword=
    @GetMapping("/search")
    public ResponseEntity<List<StudyGroup>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(studyGroupService.searchGroups(keyword));
    }
 
    // GET /api/groups/{groupId}
    @GetMapping("/{groupId}")
    public ResponseEntity<StudyGroup> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(studyGroupService.getGroup(groupId));
    }
 
    // GET /api/groups/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StudyGroup>> getGroupsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(studyGroupService.getGroupsForUser(userId));
    }
 
    // GET /api/groups/{groupId}/leaderboard
    @GetMapping("/{groupId}/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard(@PathVariable Long groupId) {
        return ResponseEntity.ok(studyGroupService.getLeaderboard(groupId));
    }
 
    // POST /api/groups
    // Body: { "title": "", "studyGoal": "", "hostId": 1, "maxMembers": 5 }
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> body) {
        try {
            String title      = (String) body.get("title");
            String studyGoal  = (String) body.getOrDefault("studyGoal", "");
            Long hostId       = Long.valueOf(body.get("hostId").toString());
            int maxMembers    = Integer.parseInt(
                    body.getOrDefault("maxMembers", StudyGroup.DEFAULT_MAX_MEMBERS).toString());
 
            StudyGroup group = studyGroupService.createGroup(title, studyGoal, hostId, maxMembers);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // POST /api/groups/{groupId}/join?userId=
    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> join(@PathVariable Long groupId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(studyGroupService.joinGroup(groupId, userId));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // POST /api/groups/{groupId}/leave?userId=
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leave(@PathVariable Long groupId, @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(studyGroupService.leaveGroup(groupId, userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // DELETE /api/groups/{groupId}?hostId=
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> disband(@PathVariable Long groupId, @RequestParam Long hostId) {
        try {
            studyGroupService.disbandGroup(groupId, hostId);
            return ResponseEntity.ok(Map.of("message", "Group disbanded."));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
 