package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AdminDashboardResponse;
import com.example.demo.service.AdminDashboardService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.demo.dto.DailyThreatActivityResponse;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(
            AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardResponse> getDashboardSummary() {

        return ResponseEntity.ok(
                adminDashboardService.getDashboardSummary());
    }
    @GetMapping("/threat-activity")
    public ResponseEntity<List<DailyThreatActivityResponse>> getDailyThreatActivity(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return ResponseEntity.ok(
                adminDashboardService.getDailyThreatActivity(date)
        );
    }
}