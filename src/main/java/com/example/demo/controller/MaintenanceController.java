
package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.CurrentUserService;
import com.example.demo.service.MaintenanceService;

@RestController
@RequestMapping("/api/admin/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final CurrentUserService currentUserService;

    public MaintenanceController(
            MaintenanceService maintenanceService,
            CurrentUserService currentUserService) {

        this.maintenanceService = maintenanceService;
        this.currentUserService = currentUserService;
    }

    // Retrieve current maintenance status
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMaintenanceStatus() {

        currentUserService.getCurrentPlatformAdmin();

        return ResponseEntity.ok(
                maintenanceService.getMaintenanceStatus()
        );
    }

    // Schedule maintenance activation
    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> scheduleMaintenance() {

        Long adminId =
                currentUserService.getCurrentPlatformAdminId();

        maintenanceService.scheduleMaintenance(adminId);

        return ResponseEntity.ok(
                maintenanceService.getMaintenanceStatus()
        );
    }

    // Cancel a scheduled maintenance activation
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelScheduledMaintenance() {

        Long adminId =
                currentUserService.getCurrentPlatformAdminId();

        maintenanceService.cancelScheduledMaintenance(adminId);

        return ResponseEntity.ok(
                maintenanceService.getMaintenanceStatus()
        );
    }

    // Manually disable active maintenance mode
    @PostMapping("/disable")
    public ResponseEntity<Map<String, Object>> disableMaintenance() {

        Long adminId =
                currentUserService.getCurrentPlatformAdminId();

        maintenanceService.disableMaintenance(adminId);

        return ResponseEntity.ok(
                maintenanceService.getMaintenanceStatus()
        );
    }
}
