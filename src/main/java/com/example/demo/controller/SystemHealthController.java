package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SystemHealthResponse;
import com.example.demo.service.SystemHealthService;

@RestController
@RequestMapping("/api/admin/system-health")
public class SystemHealthController {

    private final SystemHealthService systemHealthService;

    public SystemHealthController(
            SystemHealthService systemHealthService) {
        this.systemHealthService = systemHealthService;
    }

    @GetMapping
    public SystemHealthResponse getSystemHealth() {
        return systemHealthService.getSystemHealth();
    }
}