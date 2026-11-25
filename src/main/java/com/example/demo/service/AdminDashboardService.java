
package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AdminDashboardResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.entity.Organization;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.ThreatRepository;
import com.example.demo.repository.ThreatShareRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.dto.DailyThreatActivityResponse;
import com.example.demo.entity.Threat;

@Service
public class AdminDashboardService {

    private final OrganizationRepository organizationRepository;
    private final ThreatRepository threatRepository;
    private final ThreatShareRepository threatShareRepository;
    private final AuditLogRepository auditLogRepository;
    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardService(
            OrganizationRepository organizationRepository,
            ThreatRepository threatRepository,
            ThreatShareRepository threatShareRepository,
            AuditLogRepository auditLogRepository,
            JdbcTemplate jdbcTemplate) {

        this.organizationRepository = organizationRepository;
        this.threatRepository = threatRepository;
        this.threatShareRepository = threatShareRepository;
        this.auditLogRepository = auditLogRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdminDashboardResponse getDashboardSummary() {

        AdminDashboardResponse response = new AdminDashboardResponse();

        // Organization statistics
        response.setTotalOrganizations(organizationRepository.count());
        response.setActiveOrganizations(
                organizationRepository.countByStatus("ACTIVE"));
        response.setPendingOrganizations(
                organizationRepository.countByStatus("PENDING"));

        // Platform administrator statistics
        response.setTotalAdministrators(threatShareRepository.count());

        // Threat statistics
        response.setTotalThreats(threatRepository.count());
        response.setThreatSeverity(getThreatSeverity());

        // Recent platform activity
        response.setRecentActivity(
                auditLogRepository.findTop5ByOrderByOccurredAtDesc()
                        .stream()
                        .map(this::mapActivity)
                        .toList());

        // Recently registered organizations
        response.setRecentOrganizations(
                organizationRepository.findTop5ByOrderByCreatedAtDesc()
                        .stream()
                        .map(this::mapOrganization)
                        .toList());

        // System health
        response.setSystemHealth(getSystemHealth());

        return response;
    }

    private Map<String, Long> getThreatSeverity() {

        Map<String, Long> severityCounts = new LinkedHashMap<>();

        severityCounts.put("CRITICAL", 0L);
        severityCounts.put("HIGH", 0L);
        severityCounts.put("MEDIUM", 0L);
        severityCounts.put("LOW", 0L);

        List<Object[]> results =
                threatRepository.countThreatsBySeverity();

        for (Object[] result : results) {

            String severity = (String) result[0];
            Long count = ((Number) result[1]).longValue();

            if (severity != null) {
                severityCounts.put(severity.toUpperCase(), count);
            }
        }

        return severityCounts;
    }

    private AdminDashboardResponse.ActivityItem mapActivity(
            AuditLog auditLog) {

        AdminDashboardResponse.ActivityItem item =
                new AdminDashboardResponse.ActivityItem();

        item.setId(auditLog.getId());
        item.setActionType(auditLog.getActionType());
        item.setDescription(auditLog.getEventDescription());
        item.setActor(auditLog.getActorIdentifier());
        item.setCategory(auditLog.getEventCategory());
        item.setStatus(auditLog.getEventStatus());
        item.setOccurredAt(auditLog.getOccurredAt());

        return item;
    }

    private AdminDashboardResponse.OrganizationItem mapOrganization(
            Organization organization) {

        AdminDashboardResponse.OrganizationItem item =
                new AdminDashboardResponse.OrganizationItem();

        item.setId(organization.getId());
        item.setName(organization.getName());
        item.setStatus(organization.getStatus());
        item.setRegisteredAt(organization.getCreatedAt());

        return item;
    }

    private AdminDashboardResponse.SystemHealth getSystemHealth() {

        AdminDashboardResponse.SystemHealth health =
                new AdminDashboardResponse.SystemHealth();

        health.setBackend("UP");

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.setDatabase("UP");
        } catch (Exception exception) {
            health.setDatabase("DOWN");
        }

        // The dashboard endpoint is protected by Spring Security.
        // Reaching this service means the security configuration is active.
        health.setSecurity("UP");

        return health;
    }
    public List<DailyThreatActivityResponse> getDailyThreatActivity(
            LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime startOfNextDay = date.plusDays(1).atStartOfDay();

        List<Threat> threats =
                threatRepository
                        .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                                startOfDay,
                                startOfNextDay
                        );

        return threats.stream()
                .map(threat -> new DailyThreatActivityResponse(
                        threat.getId(),
                        threat.getTitle(),
                        threat.getSeverity(),
                        threat.getCreatedAt()
                ))
                .toList();
    }
}
