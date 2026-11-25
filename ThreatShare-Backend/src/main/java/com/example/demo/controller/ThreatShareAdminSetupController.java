
package com.example.demo.controller;

import com.example.demo.dto.ThreatShareAdminSetupRequest;
import com.example.demo.service.ThreatShareAdminSetupService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/setup")
public class ThreatShareAdminSetupController {

    private final ThreatShareAdminSetupService setupService;

    public ThreatShareAdminSetupController(
            ThreatShareAdminSetupService setupService) {
        this.setupService = setupService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> setup(
            @Valid @RequestBody ThreatShareAdminSetupRequest request) {

        setupService.setup(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message",
                       "ThreatShare platform administrator created successfully"));
    }
}
