
package com.example.demo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ThreatCreateRequest;
import com.example.demo.dto.ThreatResponse;
import com.example.demo.service.ThreatService;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.demo.dto.ThreatUpdateRequest;

@RestController
@RequestMapping("/api/organization/threats")
public class ThreatController {

    private final ThreatService threatService;

    public ThreatController(ThreatService threatService) {
        this.threatService = threatService;
    }

    // Submit a new threat with its associated indicators.
    @PostMapping
    public ResponseEntity<ThreatResponse> createThreat(
            @Valid @RequestBody ThreatCreateRequest request,
            HttpServletRequest httpRequest) {

        ThreatResponse response =
                threatService.createThreat(request, httpRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<List<ThreatResponse>> getOrganizationThreats() {

        List<ThreatResponse> threats = threatService.getOrganizationThreats();

        return ResponseEntity.ok(threats);
    }
    @GetMapping("/{threatId}")
    public ResponseEntity<ThreatResponse> getThreatById(
            @PathVariable Long threatId) {

        ThreatResponse response = threatService.getThreatById(threatId);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{threatId}")
    public ResponseEntity<ThreatResponse> updateThreat(
            @PathVariable Long threatId,
            @Valid @RequestBody ThreatUpdateRequest request,
            HttpServletRequest httpRequest) {

        ThreatResponse response = threatService.updateThreat(
                threatId,
                request,
                httpRequest
        );

        return ResponseEntity.ok(response);
    }
}
