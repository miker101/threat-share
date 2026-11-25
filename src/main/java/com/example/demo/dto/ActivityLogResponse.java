
package com.example.demo.dto;

import java.time.LocalDateTime;

public record ActivityLogResponse(
        Long id,
        String title,
        String description,
        String performedBy,
        String activityType,
        String outcome,
        LocalDateTime timestamp,
        String reference,
        String ipAddress,
        String resource,
        String actorType
) {
}
