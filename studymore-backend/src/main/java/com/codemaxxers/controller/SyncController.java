package com.codemaxxers.controller;

import com.codemaxxers.model.SyncModel;
import com.codemaxxers.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    @Autowired
    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> syncUserData(@RequestBody SyncModel payload) {
        try {
            syncService.processSync(payload);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Database synchronized successfully."
            ));
            
        } catch (Exception e) {
            System.err.println("Sync failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to synchronize database: " + e.getMessage()
            ));
        }
    }
}