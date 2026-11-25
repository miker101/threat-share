
package com.example.demo.dto;

public record InvestigationStatisticsResponse(
        long totalInvestigations,
        long activeInvestigations,
        long reviewInvestigations,
        long completedInvestigations
) {
}
