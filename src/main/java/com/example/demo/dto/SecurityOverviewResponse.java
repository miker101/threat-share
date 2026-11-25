package com.example.demo.dto;

public record SecurityOverviewResponse(

        // Main overview statistics
        long securityEvents,
        long failedLoginAttempts,
        long activeSessions,
        long administratorAccounts,

        // Authentication activity statistics
        long successfulLogins,
        long failedLogins,
        long suspiciousAttempts,

        // Protection configuration
        boolean authenticationMonitoringEnabled,
        boolean suspiciousLoginAlertsEnabled,
        int maximumLoginAttempts,
        int sessionTimeoutMinutes,
        int maximumSessions,

        // Audit integrity
        AuditIntegrityResponse auditIntegrity

) {}