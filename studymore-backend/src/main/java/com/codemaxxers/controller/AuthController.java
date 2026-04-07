package com.codemaxxers.controller;

import com.codemaxxers.model.User;
import com.codemaxxers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Map;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            // Parse the ID sent from the frontend
            Long userId = Long.valueOf(body.get("userId")); 
            
            User user = userService.register(
                userId,
                body.get("username"),
                body.get("email"),
                body.get("password")
            );
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Optional<User> user = userService.login(
            body.get("email"),
            body.get("password")
        );
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    @PostMapping("/users/sync")
    public ResponseEntity<?> syncUser(@RequestBody Map<String, Object> body) {
        try {
            Long   userId   = Long.valueOf(body.get("userId").toString());
            String username = (String) body.get("username");
            String email    = (String) body.get("email");
            String passHash = (String) body.get("passwordHash");

            return ResponseEntity.ok(userService.syncUser(userId, username, email, passHash));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

   @PostMapping("/users/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            userService.updateLastSeen(userId);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}