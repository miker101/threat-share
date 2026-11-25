
package com.example.demo.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.AuditLog;
import com.example.demo.entity.Organization;
import com.example.demo.entity.User;
import com.example.demo.entity.ThreatShare;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.security.OrganizationAdminPrincipal;
import com.example.demo.security.PlatformAdminPrincipal;

import java.time.temporal.ChronoUnit;

import com.example.demo.dto.AuditIntegrityResponse;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final EntityManager entityManager;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            EntityManager entityManager) {
        this.auditLogRepository = auditLogRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void recordSuccess(
            String actionType,
            String eventCategory,
            String resourceType,
            String resourceId,
            String description,
            Long organizationId,
            HttpServletRequest request) {

        // Serialize all audit-chain writes.
        auditLogRepository.lockAuditChain()
                .orElseThrow(() -> new IllegalStateException(
                        "Audit chain lock record is missing."));

        List<AuditLog> existingLogs =
                auditLogRepository.findAllByOrderByIdAsc();

        // Backfill the original records once, if the entire chain is unhashed.
        if (!existingLogs.isEmpty()
                && existingLogs.stream().allMatch(log ->
                        log.getEventHash() == null
                        && log.getPreviousEventHash() == null)) {

            backfillExistingLogs(existingLogs);

        } else {
            // Never silently repair a partially hashed or corrupted chain.
            verifyChainOrThrow(existingLogs);
        }

        AuditLog log = new AuditLog();

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "Cannot audit an action without an authenticated actor.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof OrganizationAdminPrincipal orgAdmin) {

            log.setActorType("ORGANIZATION_ADMIN");
            log.setActorIdentifier(orgAdmin.email());

            log.setUser(entityManager.getReference(
                    User.class, orgAdmin.userId()));

            log.setOrganization(entityManager.getReference(
                    Organization.class, orgAdmin.organizationId()));

        } else if (principal instanceof PlatformAdminPrincipal platformAdmin) {

            log.setActorType("PLATFORM_ADMIN");
            log.setActorIdentifier(platformAdmin.email());

            log.setPlatformAdmin(entityManager.getReference(
                    ThreatShare.class, platformAdmin.adminId()));

        } else {
            throw new IllegalStateException(
                    "Unsupported authenticated principal.");
        }

        if (organizationId != null) {
            log.setOrganization(entityManager.getReference(
                    Organization.class, organizationId));
        }

        log.setActionType(actionType);
        log.setEventCategory(eventCategory);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setEventDescription(description);
        log.setEventStatus("SUCCESS");
        log.setHashAlgorithm("SHA-256");
        log.setOccurredAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));

        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));

        String correlationId = request.getHeader("X-Correlation-ID");

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        log.setCorrelationId(correlationId);

        // Link this event to the previous event.
        String previousHash = existingLogs.isEmpty()
                ? null
                : existingLogs.get(existingLogs.size() - 1).getEventHash();

        log.setPreviousEventHash(previousHash);
        
     // Persist first so the database generates the event ID.
        auditLogRepository.saveAndFlush(log);

        // Reload the database-persisted values before calculating the hash.
        // This ensures the hash uses the exact timestamp and values stored by MySQL.
        entityManager.refresh(log);

        // Calculate the hash using the persisted values.
        log.setEventHash(calculateHash(log));
        auditLogRepository.saveAndFlush(log);

    }
    
    @Transactional
    public void recordAuthenticationEvent(
            String administratorType,
            Long administratorId,
            Long organizationId,
            String email,
            String sessionId,
            String eventType,
            String outcome,
            String severity,
            String description,
            String ipAddress,
            String userAgent,
            String correlationId) {

        // Lock the shared audit chain.
        auditLogRepository.lockAuditChain()
                .orElseThrow(() -> new IllegalStateException(
                        "Audit chain lock record is missing."));

        List<AuditLog> existingLogs =
                auditLogRepository.findAllByOrderByIdAsc();

        // Preserve the existing chain integrity rules.
        if (!existingLogs.isEmpty()
                && existingLogs.stream().allMatch(log ->
                        log.getEventHash() == null
                        && log.getPreviousEventHash() == null)) {

            backfillExistingLogs(existingLogs);

        } else {
            verifyChainOrThrow(existingLogs);
        }

        AuditLog log = new AuditLog();

        log.setActorType(administratorType);
        log.setActorIdentifier(email);

        // Resolve administrator relationships where possible.
        if (administratorId != null) {

            if ("PLATFORM_ADMIN".equals(administratorType)) {

                log.setPlatformAdmin(entityManager.getReference(
                        ThreatShare.class, administratorId));

            } else if ("ORGANIZATION_ADMIN".equals(administratorType)) {

                log.setUser(entityManager.getReference(
                        User.class, administratorId));
            }
        }

        if (organizationId != null) {
            log.setOrganization(entityManager.getReference(
                    Organization.class, organizationId));
        }

        log.setActionType(eventType);
        log.setEventCategory("AUTHENTICATION");
        log.setResourceType("ADMINISTRATOR_SESSION");
        log.setResourceId(sessionId);
        log.setEventDescription(description);
        log.setEventStatus(outcome);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);

        log.setHashAlgorithm("SHA-256");

        log.setOccurredAt(
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        log.setCorrelationId(correlationId);

        // Link to the previous event.
        String previousHash = existingLogs.isEmpty()
                ? null
                : existingLogs.get(existingLogs.size() - 1)
                        .getEventHash();

        log.setPreviousEventHash(previousHash);

        // Persist first to generate the database ID.
        auditLogRepository.saveAndFlush(log);

        entityManager.refresh(log);

        // Hash the persisted database values.
        log.setEventHash(calculateHash(log));

        auditLogRepository.saveAndFlush(log);
    }

@Transactional(readOnly = true)
public AuditIntegrityResponse verifyIntegrity() {

    List<AuditLog> logs =
            auditLogRepository.findAllByOrderByIdAsc();

    LocalDateTime verifiedAt = LocalDateTime.now();

    try {

        verifyChainOrThrow(logs);

        return new AuditIntegrityResponse(
                true,
                "All audit events passed integrity verification.",
                null,
                logs.size(),
                verifiedAt
        );

    } catch (AuditChainIntegrityException exception) {

        return new AuditIntegrityResponse(
                false,
                exception.getMessage(),
                exception.getFailedEventId(),
                logs.size(),
                verifiedAt
        );
    }
}


    private void backfillExistingLogs(List<AuditLog> logs) {

        String previousHash = null;

        for (AuditLog log : logs) {

            log.setPreviousEventHash(previousHash);
            log.setHashAlgorithm("SHA-256");

            String currentHash = calculateHash(log);

            log.setEventHash(currentHash);

            auditLogRepository.saveAndFlush(log);

            previousHash = currentHash;
        }
    }


private void verifyChainOrThrow(List<AuditLog> logs) {

    String expectedPreviousHash = null;

    for (AuditLog log : logs) {

        if (!"SHA-256".equals(log.getHashAlgorithm())) {
            throw new AuditChainIntegrityException(
                    "Unsupported audit hash algorithm at event ID "
                            + log.getId(),
                    log.getId()
            );
        }

        if (!Objects.equals(
                expectedPreviousHash,
                log.getPreviousEventHash())) {

            throw new AuditChainIntegrityException(
                    "Audit chain link mismatch at event ID "
                            + log.getId(),
                    log.getId()
            );
        }

        if (log.getEventHash() == null
                || !log.getEventHash().equals(calculateHash(log))) {

            throw new AuditChainIntegrityException(
                    "Audit event integrity verification failed at ID "
                            + log.getId(),
                    log.getId()
            );
        }

        expectedPreviousHash = log.getEventHash();
    }
}

    private String calculateHash(AuditLog log) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Fixed field order makes the hash deterministic.
            updateDigest(digest, log.getId());
            updateDigest(digest, log.getOrganization() == null
                    ? null : log.getOrganization().getId());
            updateDigest(digest, log.getUser() == null
                    ? null : log.getUser().getId());
            updateDigest(digest, log.getPlatformAdmin() == null
                    ? null : log.getPlatformAdmin().getId());

            updateDigest(digest, log.getActorType());
            updateDigest(digest, log.getActorIdentifier());
            updateDigest(digest, log.getActionType());
            updateDigest(digest, log.getResourceType());
            updateDigest(digest, log.getResourceId());
            updateDigest(digest, log.getEventCategory());
            updateDigest(digest, log.getEventDescription());
            updateDigest(digest, log.getIpAddress());
            updateDigest(digest, log.getUserAgent());
            updateDigest(digest, log.getEventStatus());
            updateDigest(digest, log.getPreviousEventHash());
            updateDigest(digest, log.getHashAlgorithm());
            updateDigest(digest, log.getCorrelationId());
            updateDigest(digest, log.getOccurredAt());

            return HexFormat.of().formatHex(digest.digest());

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable.", exception);
        }
    }

    // Length-prefix each field to prevent ambiguous concatenation.
    private void updateDigest(MessageDigest digest, Object value) {

        if (value == null) {
            digest.update(ByteBuffer.allocate(4).putInt(-1).array());
            return;
        }

        byte[] bytes = value.toString()
                .getBytes(StandardCharsets.UTF_8);

        digest.update(ByteBuffer.allocate(4).putInt(bytes.length).array());
        digest.update(bytes);
    }

private static class AuditChainIntegrityException
        extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final Long failedEventId;

    public AuditChainIntegrityException(
            String message,
            Long failedEventId) {

        super(message);
        this.failedEventId = failedEventId;
    }

    public Long getFailedEventId() {
        return failedEventId;
    }
}

    
}
