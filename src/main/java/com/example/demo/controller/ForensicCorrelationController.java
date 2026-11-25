
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ForensicCorrelationResponse;
import com.example.demo.service.ForensicCorrelationService;

@RestController
@RequestMapping("/api/organization/correlations")
public class ForensicCorrelationController {

    private final ForensicCorrelationService correlationService;

    public ForensicCorrelationController(
            ForensicCorrelationService correlationService) {
        this.correlationService = correlationService;
    }

    // Analyze and retrieve correlations involving the current organization.
    @GetMapping
    public ResponseEntity<List<ForensicCorrelationResponse>>
            getCorrelations() {

        return ResponseEntity.ok(
                correlationService.analyzeCorrelations()
        );
    }
}
