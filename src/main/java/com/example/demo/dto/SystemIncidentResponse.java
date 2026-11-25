package com.example.demo.dto;

import java.time.LocalDateTime;

public record SystemIncidentResponse(
        Long id,
        String component,
        String severity,
        String title,
        String description,
        String status,
        LocalDateTime firstObservedAt,
        LocalDateTime lastObservedAt,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt
) {}