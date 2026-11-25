
package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ForensicTimelineResponse(
        String indicatorType,
        String indicatorValue,
        int evidenceScore,
        String confidenceLabel,
        List<TimelineEvent> events
) {

    public record TimelineEvent(
            Long threatId,
            Long organizationId,
            String category,
            String severity,
            String threatStatus,
            LocalDateTime firstObserved,
            LocalDateTime lastObserved
    ) {}
}
