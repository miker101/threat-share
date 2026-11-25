
package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ForensicCorrelationResponse(
        String indicatorType,
        String indicatorValue,
        int evidenceScore,
        String confidenceLabel,
        String explanation,
        List<IncidentEvidence> incidents
) {

    public record IncidentEvidence(
            Long threatId,
            Long organizationId,
            String category,
            String severity,
            String threatStatus,
            BigDecimal indicatorConfidence,
            LocalDateTime firstObserved,
            LocalDateTime lastObserved
    ) {}
}
