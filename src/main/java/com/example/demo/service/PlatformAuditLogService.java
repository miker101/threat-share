
package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.PlatformAuditLogResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;

@Service
public class PlatformAuditLogService {

    private final AuditLogRepository auditLogRepository;

    public PlatformAuditLogService(
            AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<PlatformAuditLogResponse> getAllAuditLogs() {

        return auditLogRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformAuditLogResponse getAuditLogById(Long id) {

        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Audit event not found: " + id));

        return toResponse(log);
    }

    private PlatformAuditLogResponse toResponse(AuditLog log) {

        return new PlatformAuditLogResponse(
                log.getId(),

                log.getActorIdentifier(),
                log.getActorType(),

                log.getActionType(),

                log.getResourceType(),
                log.getResourceId(),

                log.getEventCategory(),
                log.getEventDescription(),
                log.getEventStatus(),

                log.getOccurredAt(),
                log.getRecordedAt(),

                log.getIpAddress(),
                log.getUserAgent(),
                log.getCorrelationId(),

                log.getOrganization() == null
                        ? null
                        : log.getOrganization().getId(),

                log.getPreviousEventHash(),
                log.getEventHash(),
                log.getHashAlgorithm()
        );
    }
}
