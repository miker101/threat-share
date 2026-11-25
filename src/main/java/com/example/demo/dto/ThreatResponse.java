package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ThreatResponse(

        Long id,
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
        LocalDateTime updatedAt,
        Long organizationId,
        Long submittedBy,
        List<ThreatIndicatorResponse> indicators

) {}