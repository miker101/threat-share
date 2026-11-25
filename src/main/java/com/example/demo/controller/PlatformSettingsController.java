
package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PlatformSetting;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.PlatformSettingsService;

@RestController
@RequestMapping("/api/admin/settings")
public class PlatformSettingsController {

    private final PlatformSettingsService settingsService;
    private final CurrentUserService currentUserService;

    public PlatformSettingsController(
            PlatformSettingsService settingsService,
            CurrentUserService currentUserService) {

        this.settingsService = settingsService;
        this.currentUserService = currentUserService;
    }

    // Retrieve all platform settings
    @GetMapping
    public ResponseEntity<List<PlatformSetting>> getAllSettings() {

        currentUserService.getCurrentPlatformAdmin();

        return ResponseEntity.ok(
                settingsService.getAllSettings()
        );
    }

    // Retrieve settings by category
    @GetMapping("/{category}")
    public ResponseEntity<List<PlatformSetting>> getSettingsByCategory(
            @PathVariable String category) {

        currentUserService.getCurrentPlatformAdmin();

        return ResponseEntity.ok(
                settingsService.getSettingsByCategory(category)
        );
    }

    // Update platform settings
    @PutMapping
    public ResponseEntity<List<PlatformSetting>> updateSettings(
            @RequestBody Map<String, String> updates) {

        Long adminId =
                currentUserService.getCurrentPlatformAdminId();

        return ResponseEntity.ok(
                settingsService.updateSettings(updates, adminId)
        );
    }
}
