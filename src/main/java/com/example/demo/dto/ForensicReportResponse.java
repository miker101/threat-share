
package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ForensicReportResponse(

        String reportReference,
        LocalDateTime generatedAt,
        Long generatedForOrganization,
        String reportTitle,
        String executiveSummary,

        int totalThreats,
        int totalCorrelations,
        int totalTimelineEvents,
        int totalActivityProfiles,

        String investigationConclusion,

        List<SubmittedThreat> submittedThreats,
        List<ForensicCorrelationResponse> correlations,
        List<ForensicTimelineResponse> timelines,
        List<ThreatActorProfileResponse> actorProfiles

) {

    public record SubmittedThreat(
            Long threatId,
            String title,
            String description,
            String category,
            String severity,
            String status,
            String sharingStatus,
            String source,
            LocalDateTime firstObserved,
            LocalDateTime lastObserved,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
