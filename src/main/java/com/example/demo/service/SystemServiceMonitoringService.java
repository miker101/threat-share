
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.MonitoredServiceResponse;
import com.example.demo.dto.ServiceMonitoringResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ThreatShareRepository;
import com.example.demo.repository.SharingRequestRepository;
import com.example.demo.repository.SharedIntelligenceRepository;

import com.example.demo.service.OrganizationAuthService;
import com.example.demo.service.AdminAuthService;

@Service
public class SystemServiceMonitoringService {

    private final HealthEndpoint healthEndpoint;
    private final UserRepository userRepository;
    private final ThreatShareRepository adminRepository;
    private final SharingRequestRepository sharingRequestRepository;
    private final SharedIntelligenceRepository sharedIntelligenceRepository;
    private final PendingOrganizationService pendingOrganizationService;
    private final OrganizationAuthService organizationAuthService;
    private final AdminAuthService adminAuthService;
    private final PasswordEncoder passwordEncoder;
    private final SystemIncidentService systemIncidentService;

    public SystemServiceMonitoringService(
            HealthEndpoint healthEndpoint,
            UserRepository userRepository,
            ThreatShareRepository adminRepository,
            SharingRequestRepository sharingRequestRepository,
            SharedIntelligenceRepository sharedIntelligenceRepository,
            PendingOrganizationService pendingOrganizationService,
            OrganizationAuthService organizationAuthService,
            AdminAuthService adminAuthService,
            PasswordEncoder passwordEncoder,
            SystemIncidentService systemIncidentService)  {

        this.healthEndpoint = healthEndpoint;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.sharingRequestRepository = sharingRequestRepository;
        this.sharedIntelligenceRepository = sharedIntelligenceRepository;
        this.pendingOrganizationService = pendingOrganizationService;
        this.organizationAuthService = organizationAuthService;
        this.adminAuthService = adminAuthService;
        this.passwordEncoder = passwordEncoder;
        this.systemIncidentService = systemIncidentService;
    }

    public ServiceMonitoringResponse monitorServices() {

        List<MonitoredServiceResponse> checks = new ArrayList<>();

        // 1. Backend Application
        checks.add(runCheck("Backend Application", () -> {

            HealthDescriptor health = healthEndpoint.health();

            String status = health.getStatus().getCode();

            if (!"UP".equalsIgnoreCase(status)) {
                throw new IllegalStateException(
                        "Application health status: " + status);
            }

            return "Spring Boot application is running.";
        }));

        // 2. MySQL Database
        checks.add(runCheck("MySQL Database", () -> {

            String status = getDatabaseStatus();

            if (!"UP".equalsIgnoreCase(status)) {
                throw new IllegalStateException(
                        "Database health status: " + status);
            }

            return "MySQL database connection is healthy.";
        }));

        // 3. REST API
        // Reaching this method confirms that this monitoring API
        // has successfully reached the backend service.
        checks.add(runCheck("REST API", () ->
                "System monitoring API is responding successfully."));

        // 4. Authentication Service
        checks.add(runCheck("Authentication Service", () -> {

            if (organizationAuthService == null
                    || adminAuthService == null) {
                throw new IllegalStateException(
                        "Authentication services are unavailable.");
            }

            // Verify that the configured password encoder works.
            // This uses a temporary value and creates no account.
            String probe = "ThreatShare-monitoring-probe";
            String encoded = passwordEncoder.encode(probe);

            if (!passwordEncoder.matches(probe, encoded)) {
                throw new IllegalStateException(
                        "Password encoder verification failed.");
            }

            // Read-only repository checks.
            long organizationAccounts = userRepository.count();
            long platformAdmins = adminRepository.count();

            return "Authentication components are available. "
                    + "Organization accounts: " + organizationAccounts
                    + ", platform administrators: " + platformAdmins;
        }));

        // 5. Organization Services
        checks.add(runCheck("Organization Services", () -> {

            int pendingOrganizations =
                    pendingOrganizationService
                            .getPendingOrganizations()
                            .size();

            return "Organization retrieval is operational. "
                    + "Pending applications: " + pendingOrganizations;
        }));

        // 6. Threat Sharing Services
        checks.add(runCheck("Threat Sharing Services", () -> {

            long sharingRequests =
                    sharingRequestRepository.count();

            long sharedIntelligence =
                    sharedIntelligenceRepository.count();

            return "Threat-sharing database access is operational. "
                    + "Sharing requests: " + sharingRequests
                    + ", published intelligence records: "
                    + sharedIntelligence;
        }));

        int totalServices = checks.size();

        long activeServices = checks.stream()
                .filter(check -> !"DOWN".equals(check.status()))
                .count();

        long downServices = checks.stream()
                .filter(check -> "DOWN".equals(check.status()))
                .count();

        long healthyServices = checks.stream()
                .filter(check -> "UP".equals(check.status()))
                .count();

        String overallStatus;

        if (downServices > 0) {
            overallStatus = "DOWN";
        } else if (healthyServices == totalServices) {
            overallStatus = "UP";
        } else {
            overallStatus = "DEGRADED";
        }

        double availability =
                (activeServices * 100.0) / totalServices;

        double averageResponseTime = checks.stream()
                .mapToDouble(MonitoredServiceResponse::responseTimeMs)
                .average()
                .orElse(0.0);

        return new ServiceMonitoringResponse(
                overallStatus,
                (int) activeServices,
                totalServices,
                round(availability),
                round(averageResponseTime),
                LocalDateTime.now(),
                checks
        );
    }

    private String getDatabaseStatus() {

        HealthDescriptor health = healthEndpoint.health();

        if (!(health instanceof CompositeHealthDescriptor composite)) {
            throw new IllegalStateException(
                    "Database health component is unavailable.");
        }

        Map<String, HealthDescriptor> components =
                composite.getComponents();

        HealthDescriptor database = components.get("db");

        if (database == null) {
            throw new IllegalStateException(
                    "Database health component was not found.");
        }

        return database.getStatus().getCode();
    }

    private MonitoredServiceResponse runCheck(
            String serviceName,
            Callable<String> operation) {

        long start = System.nanoTime();

        String status;
        String message;

        try {
            message = operation.call();
            status = "UP";

        } catch (Exception exception) {

            status = "DOWN";

            message = exception.getMessage() != null
                    ? exception.getMessage()
                    : "Service check failed.";
        }

        double responseTime =
                (System.nanoTime() - start) / 1_000_000.0;

        // Record the service state and any incident transition.
        try {
            systemIncidentService.recordServiceCheck(
                    serviceName,
                    status,
                    message
            );
        } catch (Exception incidentException) {
            System.err.println(
                    "Failed to record system incident for "
                    + serviceName + ": "
                    + incidentException.getMessage()
            );
        }

        return new MonitoredServiceResponse(
                serviceName,
                status,
                message,
                round(responseTime),
                LocalDateTime.now()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
