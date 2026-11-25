package com.example.demo.dto;

import java.time.LocalDateTime;

public record SecurityEventResponse(

        Long id,
        String administratorType,
        Long administratorId,
        Long organizationId,
        String administratorEmail,
        String sessionId,
        String eventType,
        String outcome,
        String severity,
        String ipAddress,
        String userAgent,
        String eventDescription,
        LocalDateTime occurredAt

) {}