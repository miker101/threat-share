
package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ForensicCorrelationResponse;
import com.example.demo.dto.ForensicCorrelationResponse.IncidentEvidence;
import com.example.demo.dto.ThreatActorProfileResponse;
import com.example.demo.dto.ThreatActorProfileResponse.RelatedIncident;

import java.util.stream.Collectors;

import com.example.demo.repository.InvestigationThreatRepository;

@Service
public class ThreatActorProfilingService {

    private final ForensicCorrelationService correlationService;
    private final InvestigationThreatRepository investigationThreatRepository;
    private final InvestigationService investigationService;

    public ThreatActorProfilingService(
            ForensicCorrelationService correlationService,
            InvestigationThreatRepository investigationThreatRepository,
            InvestigationService investigationService) {

        this.correlationService = correlationService;
        this.investigationThreatRepository = investigationThreatRepository;
        this.investigationService = investigationService;
    }
    @Transactional(readOnly = true)
    public List<ThreatActorProfileResponse> generateProfiles() {

        List<ForensicCorrelationResponse> correlations =
                correlationService.analyzeCorrelations();

        // Group correlations that share one or more incidents.
        List<List<ForensicCorrelationResponse>> clusters =
                new ArrayList<>();

        for (ForensicCorrelationResponse correlation : correlations) {

            Set<Long> incidentIds = getIncidentIds(correlation);

            List<Integer> matchingClusters = new ArrayList<>();

            for (int i = 0; i < clusters.size(); i++) {

                boolean overlaps = clusters.get(i).stream()
                        .anyMatch(existing ->
                                !java.util.Collections.disjoint(
                                        incidentIds,
                                        getIncidentIds(existing)
                                ));

                if (overlaps) {
                    matchingClusters.add(i);
                }
            }

            if (matchingClusters.isEmpty()) {
                List<ForensicCorrelationResponse> newCluster =
                        new ArrayList<>();
                newCluster.add(correlation);
                clusters.add(newCluster);

            } else {

                int firstIndex = matchingClusters.get(0);

                clusters.get(firstIndex).add(correlation);

                // Merge other clusters connected by this correlation.
                for (int i = matchingClusters.size() - 1; i >= 1; i--) {

                    int index = matchingClusters.get(i);

                    clusters.get(firstIndex).addAll(clusters.get(index));
                    clusters.remove(index);
                }
            }
        }

        return clusters.stream()
                .map(this::buildProfile)
                .sorted(Comparator.comparingInt(
                        ThreatActorProfileResponse::evidenceScore
                ).reversed())
                .toList();
    }

    private Set<Long> getIncidentIds(
            ForensicCorrelationResponse correlation) {

        Set<Long> ids = new LinkedHashSet<>();

        for (IncidentEvidence incident : correlation.incidents()) {
            ids.add(incident.threatId());
        }

        return ids;
    }

    private ThreatActorProfileResponse buildProfile(
            List<ForensicCorrelationResponse> cluster) {

        Map<String, IncidentEvidence> uniqueIncidents =
                new LinkedHashMap<>();

        Set<String> indicatorTypes = new TreeSet<>();
        Set<Long> organizations = new LinkedHashSet<>();

        int totalScore = 0;

        for (ForensicCorrelationResponse correlation : cluster) {

            totalScore += correlation.evidenceScore();

            indicatorTypes.add(
                    correlation.indicatorType().toUpperCase()
            );

            for (IncidentEvidence incident : correlation.incidents()) {

                String incidentKey =
                        incident.organizationId() + ":" + incident.threatId();

                uniqueIncidents.putIfAbsent(incidentKey, incident);
                organizations.add(incident.organizationId());
            }
        }

        List<IncidentEvidence> incidents =
                new ArrayList<>(uniqueIncidents.values());

        List<RelatedIncident> relatedIncidents = incidents.stream()
                .map(incident -> new RelatedIncident(
                        incident.threatId(),
                        incident.organizationId(),
                        incident.category(),
                        incident.severity(),
                        incident.threatStatus(),
                        incident.firstObserved(),
                        incident.lastObserved()
                ))
                .sorted(Comparator.comparing(
                        RelatedIncident::firstObserved,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                ))
                .toList();

        int score = cluster.isEmpty()
                ? 0
                : Math.round((float) totalScore / cluster.size());

        String confidenceLabel;

        if (score >= 80) {
            confidenceLabel = "HIGH";
        } else if (score >= 50) {
            confidenceLabel = "MEDIUM";
        } else {
            confidenceLabel = "LOW";
        }

        LocalDateTime firstObserved = incidents.stream()
                .map(IncidentEvidence::firstObserved)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime lastObserved = incidents.stream()
                .map(IncidentEvidence::lastObserved)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        String reference = generateReference(
                incidents,
                indicatorTypes
        );

        return new ThreatActorProfileResponse(
                reference,
                "Unattributed activity cluster",
                "UNATTRIBUTED",
                confidenceLabel,
                score,
                incidents.size(),
                organizations.size(),
                firstObserved,
                lastObserved,
                new ArrayList<>(indicatorTypes),
                relatedIncidents
        );
    }

    private String generateReference(
            List<IncidentEvidence> incidents,
            Set<String> indicatorTypes) {

        String identity = incidents.stream()
                .map(i -> i.organizationId() + ":" + i.threatId())
                .sorted()
                .reduce("", (a, b) -> a + "|" + b)
                + "|" + String.join(",", indicatorTypes);

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(identity.getBytes(StandardCharsets.UTF_8));

            return "PROFILE-" + HexFormat.of()
                    .formatHex(hash)
                    .substring(0, 16)
                    .toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Unable to generate profile reference", e
            );
        }
    }

@Transactional(readOnly = true)
public List<ThreatActorProfileResponse> generateInvestigationProfiles(
        Long investigationId) {

    investigationService.getInvestigation(investigationId);

    Set<Long> linkedThreatIds = investigationThreatRepository
            .findAllByInvestigation_IdOrderByLinkedAtDesc(investigationId)
            .stream()
            .map(link -> link.getThreat().getId())
            .collect(Collectors.toSet());

    if (linkedThreatIds.isEmpty()) {
        return List.of();
    }

    return generateProfiles().stream()
            .map(profile -> {

                List<RelatedIncident> filteredIncidents =
                        profile.relatedIncidents().stream()
                                .filter(incident ->
                                        linkedThreatIds.contains(
                                                incident.threatId()
                                        ))
                                .toList();

                if (filteredIncidents.isEmpty()) {
                    return null;
                }

                Set<Long> organizationIds = filteredIncidents.stream()
                        .map(RelatedIncident::organizationId)
                        .collect(Collectors.toSet());

                return new ThreatActorProfileResponse(
                		profile.profileReference(),
                        "Unattributed activity cluster",
                        "UNATTRIBUTED",
                        profile.confidenceLabel(),
                        profile.evidenceScore(),
                        filteredIncidents.size(),
                        organizationIds.size(),
                        filteredIncidents.stream()
                                .map(RelatedIncident::firstObserved)
                                .filter(java.util.Objects::nonNull)
                                .min(Comparator.naturalOrder())
                                .orElse(null),
                        filteredIncidents.stream()
                                .map(RelatedIncident::lastObserved)
                                .filter(java.util.Objects::nonNull)
                                .max(Comparator.naturalOrder())
                                .orElse(null),
                        profile.observedIndicatorTypes(),
                        filteredIncidents
                );
            })
            .filter(java.util.Objects::nonNull)
            .toList();
}

}
