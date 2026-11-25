
package com.example.demo.dto;

import java.time.LocalDateTime;

public record PlatformAuditLogResponse(

        Long id,

        String actor,
        String actorType,

        String actionType,

        String resourceType,
        String resourceId,

        String eventCategory,
        String eventDescription,
        String eventStatus,

        LocalDateTime occurredAt,
        LocalDateTime recordedAt,

        String ipAddress,
        String userAgent,
        String correlationId,

        Long organizationId,

        String previousEventHash,
        String eventHash,
        String hashAlgorithm

) {}
