
package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ThreatActorProfileResponse(
        String profileReference,
        String profileName,
        String attributionStatus,
        String confidenceLabel,
        int evidenceScore,
        int incidentCount,
        int affectedOrganizationCount,
        LocalDateTime firstObserved,
        LocalDateTime lastObserved,
        List<String> observedIndicatorTypes,
        List<RelatedIncident> relatedIncidents
) {

    public record RelatedIncident(
            Long threatId,
            Long organizationId,
            String category,
            String severity,
            String threatStatus,
            LocalDateTime firstObserved,
            LocalDateTime lastObserved
    ) {}
}
