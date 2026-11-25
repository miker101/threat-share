
package com.example.demo.dto;

import java.time.LocalDateTime;

public record InvestigationThreatResponse(
        Long threatId,
        String threatTitle,
        String category,
        String severity,
        String threatStatus,
        String relationshipType,
        String notes,
        LocalDateTime linkedAt
) {}
