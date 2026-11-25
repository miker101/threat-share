
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ThreatActorProfileResponse;
import com.example.demo.service.ThreatActorProfilingService;

@RestController
@RequestMapping("/api/organization/threat-actor-profiles")
public class ThreatActorProfilingController {

    private final ThreatActorProfilingService profilingService;

    public ThreatActorProfilingController(
            ThreatActorProfilingService profilingService) {
        this.profilingService = profilingService;
    }

    @GetMapping
    public ResponseEntity<List<ThreatActorProfileResponse>>
            getProfiles() {

        return ResponseEntity.ok(
                profilingService.generateProfiles()
        );
    }
    @GetMapping("/investigation/{investigationId}")
    public ResponseEntity<List<ThreatActorProfileResponse>> getInvestigationProfiles(
            @PathVariable Long investigationId) {

        return ResponseEntity.ok(
                profilingService.generateInvestigationProfiles(investigationId)
        );
    }
}
