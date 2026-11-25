package com.example.demo.dto;

import java.time.LocalDateTime;

public record SecuritySessionResponse(

        Long id,
        String sessionId,
        String administratorType,
        Long administratorId,
        Long organizationId,
        String email,
        String status,
        LocalDateTime createdAt,
        LocalDateTime lastActivityAt,
        LocalDateTime expiresAt,
        LocalDateTime revokedAt,
        String revocationReason,
        LocalDateTime expiredAt

) {}