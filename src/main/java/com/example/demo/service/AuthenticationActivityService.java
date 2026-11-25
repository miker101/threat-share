
package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.AuthenticationActivity;
import com.example.demo.repository.AuthenticationActivityRepository;

import jakarta.servlet.http.HttpServletRequest;


import org.springframework.transaction.annotation.Propagation;

import com.example.demo.service.AuditLogService;

@Service
public class AuthenticationActivityService {

    private final AuthenticationActivityRepository activityRepository;
    private final PlatformSettingsService platformSettingsService;
    private final AuditLogService auditLogService;

    public AuthenticationActivityService(
            AuthenticationActivityRepository activityRepository,
            PlatformSettingsService platformSettingsService,
            AuditLogService auditLogService) {

        this.activityRepository = activityRepository;
        this.platformSettingsService = platformSettingsService;
        this.auditLogService = auditLogService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuthenticationActivity recordEvent(
            String administratorType,
            Long administratorId,
            Long organizationId,
            String email,
            String sessionId,
            String eventType,
            String outcome,
            String severity,
            String description,
            HttpServletRequest request) {

        AuthenticationActivity activity = new AuthenticationActivity();

        activity.setAdministratorType(administratorType);
        activity.setAdministratorId(administratorId);
        activity.setOrganizationId(organizationId);
        activity.setAdministratorEmail(email);
        activity.setSessionId(sessionId);

        activity.setEventType(eventType);
        activity.setOutcome(outcome);
        activity.setSeverity(severity);

        activity.setEventDescription(description);

        activity.setIpAddress(resolveClientIp(request));

        if (request != null) {
            activity.setUserAgent(
                    request.getHeader("User-Agent")
            );
        }

        activity.setOccurredAt(LocalDateTime.now());

        AuthenticationActivity savedActivity =
                activityRepository.save(activity);
        auditLogService.recordAuthenticationEvent(
                savedActivity.getAdministratorType(),
                savedActivity.getAdministratorId(),
                savedActivity.getOrganizationId(),
                savedActivity.getAdministratorEmail(),
                savedActivity.getSessionId(),
                savedActivity.getEventType(),
                savedActivity.getOutcome(),
                savedActivity.getSeverity(),
                savedActivity.getEventDescription(),
                savedActivity.getIpAddress(),
                savedActivity.getUserAgent(),
                null
        );

        if ("FAILED_LOGIN".equals(eventType)
                && "FAILURE".equals(outcome)) {

            detectSuspiciousLogin(savedActivity);
        }

        return savedActivity;
    }

    private String resolveClientIp(HttpServletRequest request) {

        if (request == null) {
            return null;
        }

        return request.getRemoteAddr();
    }
    private void detectSuspiciousLogin(
            AuthenticationActivity failedActivity) {

        // Respect the platform's security monitoring settings.
        if (!platformSettingsService.getBooleanSetting(
                "loginMonitoringEnabled")) {
            return;
        }

        if (!platformSettingsService.getBooleanSetting(
                "suspiciousLoginAlertsEnabled")) {
            return;
        }

        int maximumAttempts =
                platformSettingsService.getIntegerSetting(
                        "maximumLoginAttempts");

        // Examine failed attempts from the last 15 minutes.
        LocalDateTime detectionWindow =
                LocalDateTime.now().minusMinutes(15);

        String email = failedActivity.getAdministratorEmail();
        String ipAddress = failedActivity.getIpAddress();

        long emailFailures =
                activityRepository
                        .countByAdministratorEmailAndEventTypeAndOutcomeAndOccurredAtAfter(
                                email,
                                "FAILED_LOGIN",
                                "FAILURE",
                                detectionWindow
                        );

        long ipFailures = 0;

        if (ipAddress != null && !ipAddress.isBlank()) {

            ipFailures =
                    activityRepository
                            .countByIpAddressAndEventTypeAndOutcomeAndOccurredAtAfter(
                                    ipAddress,
                                    "FAILED_LOGIN",
                                    "FAILURE",
                                    detectionWindow
                            );
        }

        // Trigger when either the account or IP reaches the threshold.
        if (emailFailures < maximumAttempts
                && ipFailures < maximumAttempts) {
            return;
        }

        // Avoid repeatedly recording the same alert for this email.
        long existingAlerts =
                activityRepository
                        .countByAdministratorEmailAndEventTypeAndOccurredAtAfter(
                                email,
                                "SECURITY",
                                detectionWindow
                        );

        if (existingAlerts > 0) {
            return;
        }

        AuthenticationActivity alert = new AuthenticationActivity();

        alert.setAdministratorType(
                failedActivity.getAdministratorType());

        alert.setAdministratorId(
                failedActivity.getAdministratorId());

        alert.setOrganizationId(
                failedActivity.getOrganizationId());

        alert.setAdministratorEmail(email);
        alert.setIpAddress(ipAddress);
        alert.setUserAgent(failedActivity.getUserAgent());

        alert.setEventType("SECURITY");
        alert.setOutcome("SUSPICIOUS");
        alert.setSeverity("HIGH");

        alert.setEventDescription(
                "Suspicious login activity detected. "
                + "Failed attempts in the last 15 minutes: "
                + "account=" + emailFailures
                + ", IP=" + ipFailures
                + ". Configured threshold=" + maximumAttempts
        );

        alert.setOccurredAt(LocalDateTime.now());

        activityRepository.save(alert);
        auditLogService.recordAuthenticationEvent(
                alert.getAdministratorType(),
                alert.getAdministratorId(),
                alert.getOrganizationId(),
                alert.getAdministratorEmail(),
                null,
                alert.getEventType(),
                alert.getOutcome(),
                alert.getSeverity(),
                alert.getEventDescription(),
                alert.getIpAddress(),
                alert.getUserAgent(),
                null
        );
    }
}
