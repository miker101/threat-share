package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SystemIncidentResponse;
import com.example.demo.service.SystemIncidentService;

@RestController
@RequestMapping("/api/admin/system-health/incidents")
public class SystemIncidentController {

    private final SystemIncidentService incidentService;

    public SystemIncidentController(
            SystemIncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<SystemIncidentResponse> getRecentIncidents() {
        return incidentService.getRecentIncidents();
    }
}