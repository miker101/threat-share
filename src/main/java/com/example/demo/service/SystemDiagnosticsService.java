
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.SystemDiagnosticCheck;
import com.example.demo.dto.SystemDiagnosticsResponse;

@Service
public class SystemDiagnosticsService {

    private final HealthEndpoint healthEndpoint;

    public SystemDiagnosticsService(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    public SystemDiagnosticsResponse runDiagnostics() {

        List<SystemDiagnosticCheck> checks = new ArrayList<>();

        LocalDateTime executedAt = LocalDateTime.now();

        // 1. Inspect application health
        HealthDescriptor health = healthEndpoint.health();

        String applicationStatus = health.getStatus().getCode();

        checks.add(new SystemDiagnosticCheck(
                "Application Health",
                applicationStatus,
                applicationStatus.equalsIgnoreCase("UP")
                        ? "Spring Boot application health checks are passing."
                        : "Application health reported: " + applicationStatus,
                executedAt
        ));

        // 2. Inspect database health
        Map<String, HealthDescriptor> components =
                health instanceof CompositeHealthDescriptor composite
                        ? java.util.Optional.ofNullable(composite.getComponents())
                                .orElse(Map.of())
                        : Map.of();

        HealthDescriptor database = components.get("db");

        String databaseStatus = database == null
                ? "UNKNOWN"
                : database.getStatus().getCode();

        checks.add(new SystemDiagnosticCheck(
                "Database Connectivity",
                databaseStatus,
                databaseStatus.equalsIgnoreCase("UP")
                        ? "MySQL database health check passed."
                        : "MySQL database status: " + databaseStatus,
                executedAt
        ));

        // 3. Inspect API availability
        // Reaching this method means the diagnostics API request
        // successfully reached the backend controller.
        checks.add(new SystemDiagnosticCheck(
                "API Availability",
                "UP",
                "The authenticated diagnostics API request reached the backend successfully.",
                executedAt
        ));

        // 4. Inspect authentication
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean authenticated =
                authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        checks.add(new SystemDiagnosticCheck(
                "Authentication",
                authenticated ? "UP" : "DOWN",
                authenticated
                        ? "The current request passed platform authentication."
                        : "No authenticated user was found for this request.",
                executedAt
        ));

        // Calculate overall diagnostic status
        boolean hasDownCheck = checks.stream()
                .anyMatch(check -> check.status().equalsIgnoreCase("DOWN"));

        boolean allChecksUp = checks.stream()
                .allMatch(check -> check.status().equalsIgnoreCase("UP"));

        String overallStatus = hasDownCheck
                ? "DOWN"
                : allChecksUp ? "UP" : "DEGRADED";

        String summary = allChecksUp
                ? "All four diagnostic checks passed."
                : "One or more diagnostic checks require attention.";

        return new SystemDiagnosticsResponse(
                overallStatus,
                summary,
                executedAt,
                checks
        );
    }
}
