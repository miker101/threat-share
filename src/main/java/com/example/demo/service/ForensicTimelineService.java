
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.ForensicCorrelationResponse;
import com.example.demo.dto.ForensicTimelineResponse;
import com.example.demo.dto.ForensicTimelineResponse.TimelineEvent;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.repository.InvestigationThreatRepository;

@Service
public class ForensicTimelineService {

    private final ForensicCorrelationService correlationService;
    private final InvestigationThreatRepository investigationThreatRepository;
    private final InvestigationService investigationService;

    public ForensicTimelineService(
            ForensicCorrelationService correlationService,
            InvestigationThreatRepository investigationThreatRepository,
            InvestigationService investigationService) {

        this.correlationService = correlationService;
        this.investigationThreatRepository = investigationThreatRepository;
        this.investigationService = investigationService;
    }

    public List<ForensicTimelineResponse> reconstructTimelines() {

        List<ForensicCorrelationResponse> correlations =
                correlationService.analyzeCorrelations();

        List<ForensicTimelineResponse> timelines =
                new ArrayList<>();

        for (ForensicCorrelationResponse correlation : correlations) {

            List<TimelineEvent> events =
                    correlation.incidents().stream()
                            .map(incident -> new TimelineEvent(
                                    incident.threatId(),
                                    incident.organizationId(),
                                    incident.category(),
                                    incident.severity(),
                                    incident.threatStatus(),
                                    incident.firstObserved(),
                                    incident.lastObserved()
                            ))
                            .sorted(Comparator.comparing(
                                    TimelineEvent::firstObserved,
                                    Comparator.nullsLast(
                                            Comparator.naturalOrder()
                                    )
                            ))
                            .toList();

            timelines.add(new ForensicTimelineResponse(
                    correlation.indicatorType(),
                    correlation.indicatorValue(),
                    correlation.evidenceScore(),
                    correlation.confidenceLabel(),
                    events
            ));
        }

        return timelines;
    }

public List<ForensicTimelineResponse> reconstructInvestigationTimeline(
        Long investigationId) {

    // Verify that the investigation belongs to the authenticated organization.
    investigationService.getInvestigation(investigationId);

    // Get only threats linked to this investigation.
    Set<Long> linkedThreatIds = investigationThreatRepository
            .findAllByInvestigation_IdOrderByLinkedAtDesc(investigationId)
            .stream()
            .map(link -> link.getThreat().getId())
            .collect(Collectors.toSet());

    if (linkedThreatIds.isEmpty()) {
        return List.of();
    }

    // Reuse the existing correlation and timeline reconstruction.
    return reconstructTimelines().stream()
            .map(timeline -> {
                List<TimelineEvent> filteredEvents = timeline.events()
                        .stream()
                        .filter(event ->
                                linkedThreatIds.contains(event.threatId()))
                        .sorted(Comparator.comparing(
                                TimelineEvent::firstObserved,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                        .toList();

                if (filteredEvents.isEmpty()) {
                    return null;
                }

                return new ForensicTimelineResponse(
                        timeline.indicatorType(),
                        timeline.indicatorValue(),
                        timeline.evidenceScore(),
                        timeline.confidenceLabel(),
                        filteredEvents
                );
            })
            .filter(java.util.Objects::nonNull)
            .toList();
}

}
