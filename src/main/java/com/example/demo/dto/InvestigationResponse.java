
package com.example.demo.dto;

import java.time.LocalDateTime;

public record InvestigationResponse(
        Long id,
        String reference,
        String title,
        String description,
        String investigationType,
        String priority,
        String status,
        String leadInvestigator,
        LocalDateTime startedAt,
        LocalDateTime closedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
