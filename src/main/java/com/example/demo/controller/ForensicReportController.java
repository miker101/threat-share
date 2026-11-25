
package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ForensicReportResponse;
import com.example.demo.service.ForensicReportService;

@RestController
@RequestMapping("/api/organization/forensic-reports")
public class ForensicReportController {

    private final ForensicReportService reportService;

    public ForensicReportController(
            ForensicReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/generate")
    public ResponseEntity<ForensicReportResponse> generateReport() {

        return ResponseEntity.ok(
                reportService.generateReport()
        );
    }
}
