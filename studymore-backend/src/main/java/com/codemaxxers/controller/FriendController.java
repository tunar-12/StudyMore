package com.codemaxxers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.codemaxxers.model.FriendRequest;
import com.codemaxxers.model.User;
import com.codemaxxers.service.FriendService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
 
    private final FriendService friendService;
 
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }
 
    // GET /api/friends/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getFriends(userId));
    }
 
    // GET /api/friends/search?keyword= &requestingUserId= 
    @GetMapping("/search")
    public ResponseEntity<List<User>> search(
            @RequestParam String keyword,
            @RequestParam Long requestingUserId) {
        return ResponseEntity.ok(friendService.searchUsers(keyword, requestingUserId));
    }
 
    // GET /api/friends/requests/pending?userId=
    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingRequests(@RequestParam Long userId) {
        try {
            List<FriendRequest> requests = friendService.getPendingRequests(userId);
            List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
            for (FriendRequest fr : requests) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("requestId", fr.getRequestId());
                map.put("senderId", fr.getSenderId());
                map.put("senderUsername", fr.getSenderUsername());
                map.put("receiverId", fr.getReceiverId());
                map.put("status", fr.getStatus().toString());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
 
    // POST /api/friends/requests
    // Body: { "senderId": 1, "receiverId": 2 }
    @PostMapping("/requests")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, Long> body) {
        try {
            FriendRequest req = friendService.sendRequest(
                    body.get("senderId"), body.get("receiverId"));
            return ResponseEntity.ok(req);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(
            @PathVariable Long requestId,
            @RequestParam Long receiverId) {
        try {
            FriendRequest req = friendService.acceptRequest(requestId, receiverId);
            return ResponseEntity.ok(req);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
 
    // PUT /api/friends/requests/{requestId}/deny?receiverId=
    @PutMapping("/requests/{requestId}/deny")
    public ResponseEntity<?> denyRequest(
            @PathVariable Long requestId,
            @RequestParam Long receiverId) {
        try {
            return ResponseEntity.ok(friendService.denyRequest(requestId, receiverId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}