
package com.example.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.SecurityOverviewResponse;
import com.example.demo.repository.AuthenticationActivityRepository;
import com.example.demo.repository.AdministratorSessionRepository;
import com.example.demo.repository.ThreatShareRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.demo.dto.SecurityEventResponse;
import com.example.demo.entity.AuthenticationActivity;

import com.example.demo.dto.SecuritySessionResponse;
import com.example.demo.entity.AdministratorSession;
import java.util.List;
@Service
@Transactional(readOnly = true)
public class SecurityMonitoringService {

    private final AuthenticationActivityRepository activityRepository;
    private final AdministratorSessionRepository sessionRepository;
    private final ThreatShareRepository adminRepository;
    private final UserRepository userRepository;
    private final PlatformSettingsService platformSettingsService;
    private final AuditLogService auditLogService;

    public SecurityMonitoringService(
            AuthenticationActivityRepository activityRepository,
            AdministratorSessionRepository sessionRepository,
            ThreatShareRepository adminRepository,
            UserRepository userRepository,
            PlatformSettingsService platformSettingsService,
            AuditLogService auditLogService) {

        this.activityRepository = activityRepository;
        this.sessionRepository = sessionRepository;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.platformSettingsService = platformSettingsService;
        this.auditLogService = auditLogService;
    }

    public SecurityOverviewResponse getOverview() {

        LocalDateTime now = LocalDateTime.now();

        long totalSecurityEvents =
                activityRepository.count();

        long failedLogins =
                activityRepository.countByEventTypeAndOutcome(
                        "FAILED_LOGIN", "FAILURE");

        long successfulLogins =
                activityRepository.countByEventTypeAndOutcome(
                        "LOGIN", "SUCCESS");

        long suspiciousAttempts =
                activityRepository.countByEventTypeAndOutcome(
                        "SECURITY", "SUSPICIOUS");

        long activeSessions =
                sessionRepository.countByStatusAndExpiresAtAfter(
                        "ACTIVE", now);

        long platformAdmins =
                adminRepository.countByActiveTrue();

        long organizationAdmins =
                userRepository.countByStatus("ACTIVE");

        long administratorAccounts =
                platformAdmins + organizationAdmins;

        boolean loginMonitoringEnabled =
                platformSettingsService.getBooleanSetting(
                        "loginMonitoringEnabled");

        boolean suspiciousAlertsEnabled =
                platformSettingsService.getBooleanSetting(
                        "suspiciousLoginAlertsEnabled");

        int maximumLoginAttempts =
                platformSettingsService.getIntegerSetting(
                        "maximumLoginAttempts");

        int sessionTimeoutMinutes =
                platformSettingsService.getIntegerSetting(
                        "sessionTimeoutMinutes");

        int maximumSessions =
                platformSettingsService.getIntegerSetting(
                        "maximumSessions");

        return new SecurityOverviewResponse(
                totalSecurityEvents,
                failedLogins,
                activeSessions,
                administratorAccounts,
                successfulLogins,
                failedLogins,
                suspiciousAttempts,
                loginMonitoringEnabled,
                suspiciousAlertsEnabled,
                maximumLoginAttempts,
                sessionTimeoutMinutes,
                maximumSessions,
                auditLogService.verifyIntegrity()
        );
    }
    public Page<SecurityEventResponse> getSecurityEvents(
            String eventType,
            String severity,
            String search,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative.");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100.");
        }

        eventType = normalizeFilter(eventType);
        severity = normalizeFilter(severity);
        search = normalizeFilter(search);

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "occurredAt"));

        return activityRepository.searchSecurityEvents(
                eventType,
                severity,
                search,
                pageable
        ).map(this::toSecurityEventResponse);
    }

    private String normalizeFilter(String value) {

        if (value == null || value.isBlank()
                || "ALL".equalsIgnoreCase(value)) {
            return null;
        }

        return value.trim();
    }

    private SecurityEventResponse toSecurityEventResponse(
            AuthenticationActivity activity) {

        return new SecurityEventResponse(
                activity.getId(),
                activity.getAdministratorType(),
                activity.getAdministratorId(),
                activity.getOrganizationId(),
                activity.getAdministratorEmail(),
                activity.getSessionId(),
                activity.getEventType(),
                activity.getOutcome(),
                activity.getSeverity(),
                activity.getIpAddress(),
                activity.getUserAgent(),
                activity.getEventDescription(),
                activity.getOccurredAt()
        );
    }
    public List<SecuritySessionResponse> getSecuritySessions(
            String status) {

        List<AdministratorSession> sessions;

        if (status == null || status.isBlank()
                || "ALL".equalsIgnoreCase(status)) {

            sessions = sessionRepository
                    .findAllByOrderByCreatedAtDesc();

        } else {

            sessions = sessionRepository
                    .findByStatusOrderByCreatedAtDesc(
                            status.trim().toUpperCase());
        }

        return sessions.stream()
                .map(this::toSecuritySessionResponse)
                .toList();
    }

    private SecuritySessionResponse toSecuritySessionResponse(
            AdministratorSession session) {

        return new SecuritySessionResponse(
                session.getId(),
                session.getSessionId(),
                session.getAdministratorType(),
                session.getAdministratorId(),
                session.getOrganizationId(),
                session.getEmail(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getLastActivityAt(),
                session.getExpiresAt(),
                session.getRevokedAt(),
                session.getRevocationReason(),
                session.getExpiredAt()
        );
    }
}
