
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ForensicCorrelationResponse;
import com.example.demo.dto.ForensicReportResponse;
import com.example.demo.dto.ForensicReportResponse.SubmittedThreat;
import com.example.demo.dto.ForensicTimelineResponse;
import com.example.demo.dto.ThreatActorProfileResponse;
import com.example.demo.entity.Threat;
import com.example.demo.repository.ThreatRepository;
import com.example.demo.security.CurrentUserService;

@Service
public class ForensicReportService {

    private final ForensicCorrelationService correlationService;
    private final ForensicTimelineService timelineService;
    private final ThreatActorProfilingService profilingService;
    private final CurrentUserService currentUserService;
    private final ThreatRepository threatRepository;

    public ForensicReportService(
            ForensicCorrelationService correlationService,
            ForensicTimelineService timelineService,
            ThreatActorProfilingService profilingService,
            CurrentUserService currentUserService,
            ThreatRepository threatRepository) {

        this.correlationService = correlationService;
        this.timelineService = timelineService;
        this.profilingService = profilingService;
        this.currentUserService = currentUserService;
        this.threatRepository = threatRepository;
    }

    @Transactional(readOnly = true)
    public ForensicReportResponse generateReport() {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        // Retrieve only this authenticated organization's threats.
        List<Threat> threats =
                threatRepository
                        .findAllByOrganization_IdOrderByCreatedAtDesc(
                                organizationId
                        );

        // Convert entities into safe report DTOs.
        List<SubmittedThreat> submittedThreats = threats.stream()
                .map(this::mapThreat)
                .toList();

        // Collect cross-organization analysis results.
        List<ForensicCorrelationResponse> correlations =
                correlationService.analyzeCorrelations();

        List<ForensicTimelineResponse> timelines =
                timelineService.reconstructTimelines();

        List<ThreatActorProfileResponse> profiles =
                profilingService.generateProfiles();

        int totalTimelineEvents = timelines.stream()
                .mapToInt(timeline -> timeline.events().size())
                .sum();

        String summary = buildExecutiveSummary(
                threats.size(),
                correlations.size(),
                totalTimelineEvents,
                profiles.size()
        );

        String conclusion = buildConclusion(
                threats.size(),
                correlations.size(),
                profiles.size()
        );

        String reportReference = "TS-FORENSIC-"
                + organizationId + "-"
                + System.currentTimeMillis();

        return new ForensicReportResponse(
                reportReference,
                LocalDateTime.now(),
                organizationId,
                "ThreatShare Organization Forensic Report",
                summary,
                threats.size(),
                correlations.size(),
                totalTimelineEvents,
                profiles.size(),
                conclusion,
                submittedThreats,
                correlations,
                timelines,
                profiles
        );
    }

    private SubmittedThreat mapThreat(Threat threat) {

        return new SubmittedThreat(
                threat.getId(),
                threat.getTitle(),
                threat.getDescription(),
                threat.getCategory(),
                threat.getSeverity(),
                threat.getStatus(),
                threat.getSharingStatus(),
                threat.getSource(),
                threat.getFirstObserved(),
                threat.getLastObserved(),
                threat.getCreatedAt(),
                threat.getUpdatedAt()
        );
    }

    private String buildExecutiveSummary(
            int threats,
            int correlations,
            int timelineEvents,
            int profiles) {

        return "This report contains "
                + threats + " threat record(s) submitted by the organization, "
                + correlations + " cross-organization indicator correlation(s), "
                + timelineEvents + " reconstructed timeline event(s), and "
                + profiles + " unattributed activity profile(s). "
                + "The report includes available organization threat records "
                + "regardless of whether cross-organization matches were found.";
    }

    private String buildConclusion(
            int threats,
            int correlations,
            int profiles) {

        if (threats == 0 && correlations == 0) {
            return "No threat records or cross-organization correlations "
                    + "were available for this organization at the time "
                    + "of report generation.";
        }

        if (correlations == 0) {
            return "The organization has "
                    + threats + " submitted threat record(s). "
                    + "No cross-organization indicator correlations "
                    + "were identified in the currently eligible shared data. "
                    + "This does not establish that no related threats exist.";
        }

        return "The organization has "
                + threats + " submitted threat record(s). "
                + correlations + " potential cross-organization correlation(s) "
                + "and " + profiles + " unattributed activity profile(s) "
                + "were identified. These findings do not independently "
                + "establish threat actor identity. Further investigation "
                + "and corroborating evidence are required before attribution.";
    }
}
