package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiPerformanceResponse;
import com.example.demo.service.ApiPerformanceService;

@RestController
@RequestMapping("/api/admin/system-health/performance")
public class ApiPerformanceController {

    private final ApiPerformanceService performanceService;

    public ApiPerformanceController(
            ApiPerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping
    public ApiPerformanceResponse getApiPerformance() {
        return performanceService.getApiPerformance();
    }
}