
package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.MaintenanceService;

@RestController
@RequestMapping("/api/maintenance")
public class PublicMaintenanceController {

    private final MaintenanceService maintenanceService;

    public PublicMaintenanceController(
            MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMaintenanceStatus() {

        Map<String, Object> status =
                maintenanceService.getMaintenanceStatus();

        return ResponseEntity.ok(status);
    }
}
