
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.AdministratorSession;
import com.example.demo.repository.AdministratorSessionRepository;

import java.util.List;

@Service
public class AdministratorSessionService {

	private final AdministratorSessionRepository sessionRepository;
	private final AuthenticationActivityService activityService;

	public AdministratorSessionService(
	        AdministratorSessionRepository sessionRepository,
	        AuthenticationActivityService activityService) {

	    this.sessionRepository = sessionRepository;
	    this.activityService = activityService;
	}

    @Transactional
    public AdministratorSession createSession(
            String administratorType,
            Long administratorId,
            Long organizationId,
            String email,
            long expirationMillis) {

        LocalDateTime now = LocalDateTime.now();

        AdministratorSession session = new AdministratorSession();

        session.setSessionId(UUID.randomUUID().toString());
        session.setAdministratorType(administratorType);
        session.setAdministratorId(administratorId);
        session.setOrganizationId(organizationId);
        session.setEmail(email);
        session.setStatus("ACTIVE");
        session.setCreatedAt(now);
        session.setLastActivityAt(now);
        session.setExpiresAt(
                now.plusNanos(expirationMillis * 1_000_000L)
        );

        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public boolean isSessionActive(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        return sessionRepository.findBySessionId(sessionId)
                .filter(session -> "ACTIVE".equals(session.getStatus()))
                .filter(session -> session.getExpiresAt().isAfter(
                        LocalDateTime.now()))
                .isPresent();
    }
    
    @Transactional
    public void updateLastActivity(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        sessionRepository.findBySessionId(sessionId)
                .filter(session -> "ACTIVE".equals(session.getStatus()))
                .filter(session -> session.getExpiresAt()
                        .isAfter(LocalDateTime.now()))
                .ifPresent(session -> {
                    session.setLastActivityAt(LocalDateTime.now());
                    sessionRepository.save(session);
                });
    }

@Transactional
public boolean revokeSession(
        String sessionId,
        String reason) {

    if (sessionId == null || sessionId.isBlank()) {
        return false;
    }

    return sessionRepository.findBySessionId(sessionId)
            .filter(session -> "ACTIVE".equals(session.getStatus()))
            .map(session -> {

                session.setStatus("REVOKED");
                session.setRevokedAt(LocalDateTime.now());
                session.setRevocationReason(reason);

                sessionRepository.save(session);

                return true;
            })
            .orElse(false);
}


    @Transactional
    public void revokeAllOrganizationAdminSessions(String reason) {

        var sessions = sessionRepository
                .findByAdministratorTypeAndStatus(
                        "ORGANIZATION_ADMIN",
                        "ACTIVE"
                );

        LocalDateTime now = LocalDateTime.now();

        for (AdministratorSession session : sessions) {

            session.setStatus("REVOKED");
            session.setRevokedAt(now);
            session.setRevocationReason(reason);
        }

        sessionRepository.saveAll(sessions);
    }
    @Transactional
    public int expireOverdueSessions() {

        LocalDateTime now = LocalDateTime.now();

        List<AdministratorSession> expiredSessions =
                sessionRepository.findByStatusAndExpiresAtLessThanEqual(
                        "ACTIVE", now
                );

        if (expiredSessions.isEmpty()) {
            return 0;
        }

        for (AdministratorSession session : expiredSessions) {

            session.setStatus("EXPIRED");
            session.setExpiredAt(now);
        }

        sessionRepository.saveAll(expiredSessions);

        for (AdministratorSession session : expiredSessions) {

            activityService.recordEvent(
                    session.getAdministratorType(),
                    session.getAdministratorId(),
                    session.getOrganizationId(),
                    session.getEmail(),
                    session.getSessionId(),
                    "SESSION",
                    "EXPIRED",
                    "INFO",
                    "Administrator session expired automatically.",
                    null
            );
        }

        return expiredSessions.size();
    }
}
