package com.example.demo.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SecurityEventResponse;
import com.example.demo.dto.SecurityOverviewResponse;
import com.example.demo.dto.SecuritySessionResponse;
import com.example.demo.service.SecurityMonitoringService;

@RestController
@RequestMapping("/api/admin/security")
public class PlatformSecurityController {

    private final SecurityMonitoringService securityMonitoringService;

    public PlatformSecurityController(
            SecurityMonitoringService securityMonitoringService) {

        this.securityMonitoringService = securityMonitoringService;
    }

    // Security dashboard overview
    @GetMapping("/overview")
    public SecurityOverviewResponse getOverview() {

        return securityMonitoringService.getOverview();
    }

    // Security events with search, filters and pagination
    @GetMapping("/events")
    public Page<SecurityEventResponse> getSecurityEvents(

            @RequestParam(required = false) String eventType,

            @RequestParam(required = false) String severity,

            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        return securityMonitoringService.getSecurityEvents(
                eventType,
                severity,
                search,
                page,
                size
        );
    }

    // Administrator sessions
    @GetMapping("/sessions")
    public List<SecuritySessionResponse> getSecuritySessions(

            @RequestParam(required = false) String status) {

        return securityMonitoringService.getSecuritySessions(status);
    }
}