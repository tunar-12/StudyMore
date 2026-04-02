package com.codemaxxers.controller;

import com.codemaxxers.model.Settings;
import com.codemaxxers.model.User;
import com.codemaxxers.repository.SettingsRepository;
import com.codemaxxers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class SettingsController {

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{userId}/settings")
    public ResponseEntity<?> getSettings(@PathVariable Long userId) {
        Optional<Settings> settings = settingsRepository.findByUserUserId(userId);
        if (settings.isPresent()) {
            return ResponseEntity.ok(settings.get());
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Settings defaultSettings = new Settings(user.get());
        return ResponseEntity.ok(settingsRepository.save(defaultSettings));
    }

    @PutMapping("/{userId}/settings")
    public ResponseEntity<?> updateSettings(@PathVariable Long userId,
                                            @RequestBody Settings updatedSettings) {
        Optional<Settings> existing = settingsRepository.findByUserUserId(userId);
        Settings settings;
        if (existing.isPresent()) {
            settings = existing.get();
        } else {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            settings = new Settings(user.get());
        }
        settings.setDarkMode(updatedSettings.isDarkMode());
        settings.setLockInMode(updatedSettings.isLockInMode());
        settings.setMascotVisible(updatedSettings.isMascotVisible());
        settings.setStudyTime(updatedSettings.getStudyTime());
        settings.setShortBreak(updatedSettings.getShortBreak());
        settings.setLongBreak(updatedSettings.getLongBreak());
        settings.setLongBreakAfter(updatedSettings.getLongBreakAfter());
        settings.setStartSound(updatedSettings.isStartSound());
        settings.setBreakAlert(updatedSettings.isBreakAlert());
        settings.setPopups(updatedSettings.isPopups());
        return ResponseEntity.ok(settingsRepository.save(settings));
    }
}