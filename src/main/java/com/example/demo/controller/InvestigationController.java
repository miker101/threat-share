
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.InvestigationResponse;
import com.example.demo.service.InvestigationService;

import com.example.demo.dto.InvestigationStatisticsResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.demo.dto.InvestigationCreateRequest;
import java.util.List;
import com.example.demo.dto.InvestigationThreatResponse;
import org.springframework.http.HttpStatus;
import com.example.demo.dto.InvestigationThreatLinkRequest;
import com.example.demo.dto.InvestigationThreatResponse;

@RestController
@RequestMapping("/api/organization/investigations")
public class InvestigationController {

    private final InvestigationService investigationService;

    public InvestigationController(
            InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    // GET /api/organization/investigations
    @GetMapping
    public ResponseEntity<List<InvestigationResponse>> getInvestigations() {

        return ResponseEntity.ok(
                investigationService.getOrganizationInvestigations()
        );
    }

    // GET /api/organization/investigations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<InvestigationResponse> getInvestigation(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                investigationService.getInvestigation(id)
        );
    }

    // GET /api/organization/investigations/statistics
    @GetMapping("/statistics")
    public ResponseEntity<InvestigationStatisticsResponse> getStatistics() {

        return ResponseEntity.ok(
                investigationService.getInvestigationStatistics()
        );
    }

    @PostMapping
    public ResponseEntity<InvestigationResponse> createInvestigation(
            @Valid @RequestBody InvestigationCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investigationService.createInvestigation(request));
    }

@GetMapping("/{investigationId}/threats")
public ResponseEntity<List<InvestigationThreatResponse>> getInvestigationThreats(
        @PathVariable Long investigationId) {

    return ResponseEntity.ok(
            investigationService.getInvestigationThreats(investigationId)
    );
}

@PostMapping("/{investigationId}/threats")
public ResponseEntity<InvestigationThreatResponse> linkThreat(
        @PathVariable Long investigationId,
        @Valid @RequestBody InvestigationThreatLinkRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED).body(
            investigationService.linkThreatToInvestigation(
                    investigationId, request
            )
    );
}
@GetMapping("/{investigationId}/evidence")
public ResponseEntity<List<InvestigationService.EvidenceResponse>> getInvestigationEvidence(
        @PathVariable Long investigationId) {

    return ResponseEntity.ok(
            investigationService.getInvestigationEvidence(investigationId)
    );
}




}
