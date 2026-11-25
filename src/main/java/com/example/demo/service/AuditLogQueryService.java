
package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ActivityLogResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;

@Service
public class AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogQueryService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getOrganizationActivities(
            Long organizationId) {

        return auditLogRepository
                .findAllByOrganization_IdOrderByOccurredAtDesc(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ActivityLogResponse toResponse(AuditLog log) {

        String performedBy = log.getActorIdentifier();

        if (performedBy == null || performedBy.isBlank()) {
            performedBy = log.getActorType();
        }

        String reference = log.getCorrelationId();

        if (reference == null || reference.isBlank()) {
            reference = log.getResourceId();
        }

        if (reference == null || reference.isBlank()) {
            reference = "AUDIT-" + log.getId();
        }

        String resource = log.getResourceType();

        if (log.getResourceId() != null
                && !log.getResourceId().isBlank()) {
            resource = (resource == null || resource.isBlank())
                    ? log.getResourceId()
                    : resource + " #" + log.getResourceId();
        }

        return new ActivityLogResponse(
                log.getId(),
                log.getActionType(),
                log.getEventDescription(),
                performedBy,
                log.getEventCategory(),
                log.getEventStatus(),
                log.getOccurredAt(),
                reference,
                log.getIpAddress(),
                resource,
                log.getActorType()
        );
    }
}
