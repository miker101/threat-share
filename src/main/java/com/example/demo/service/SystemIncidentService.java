package com.example.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.SystemIncident;
import com.example.demo.repository.SystemIncidentRepository;

import java.util.List;

import com.example.demo.dto.SystemIncidentResponse;

@Service
public class SystemIncidentService {

    private final SystemIncidentRepository incidentRepository;

    public SystemIncidentService(
            SystemIncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public void recordServiceCheck(
            String component,
            String status,
            String message) {

        LocalDateTime now = LocalDateTime.now();

        var existingIncident = incidentRepository
                .findFirstByComponentAndStatusOrderByCreatedAtDesc(
                        component,
                        "OPEN"
                );

        if ("DOWN".equalsIgnoreCase(status)) {

            if (existingIncident.isPresent()) {

                // Update the existing incident.
                SystemIncident incident = existingIncident.get();

                incident.setLastObservedAt(now);
                incident.setUpdatedAt(now);
                incident.setDescription(message);

                incidentRepository.save(incident);

            } else {

                // Create a new incident.
                SystemIncident incident = new SystemIncident();

                incident.setComponent(component);
                incident.setSeverity(determineSeverity(component));
                incident.setTitle(component + " unavailable");
                incident.setDescription(message);
                incident.setStatus("OPEN");
                incident.setFirstObservedAt(now);
                incident.setLastObservedAt(now);
                incident.setCreatedAt(now);
                incident.setUpdatedAt(now);

                incidentRepository.save(incident);
            }

        } else if ("UP".equalsIgnoreCase(status)
                && existingIncident.isPresent()) {

            // Resolve an existing incident after recovery.
            SystemIncident incident = existingIncident.get();

            incident.setStatus("RESOLVED");
            incident.setResolvedAt(now);
            incident.setUpdatedAt(now);

            incidentRepository.save(incident);
        }
    }
    
    @Transactional(readOnly = true)
    public List<SystemIncidentResponse> getRecentIncidents() {

        return incidentRepository
                .findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(incident -> new SystemIncidentResponse(
                        incident.getId(),
                        incident.getComponent(),
                        incident.getSeverity(),
                        incident.getTitle(),
                        incident.getDescription(),
                        incident.getStatus(),
                        incident.getFirstObservedAt(),
                        incident.getLastObservedAt(),
                        incident.getResolvedAt(),
                        incident.getCreatedAt()
                ))
                .toList();
    }

    private String determineSeverity(String component) {

        return switch (component) {
            case "Backend Application", "MySQL Database" -> "CRITICAL";
            case "Authentication Service", "REST API" -> "HIGH";
            default -> "MEDIUM";
        };
    }
}