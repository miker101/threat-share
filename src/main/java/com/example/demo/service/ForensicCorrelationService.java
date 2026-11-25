
package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ForensicCorrelationResponse;
import com.example.demo.dto.ForensicCorrelationResponse.IncidentEvidence;
import com.example.demo.entity.Threat;
import com.example.demo.entity.ThreatIndicator;
import com.example.demo.repository.ThreatIndicatorRepository;
import com.example.demo.security.CurrentUserService;

@Service
public class ForensicCorrelationService {

    private final ThreatIndicatorRepository indicatorRepository;
    private final CurrentUserService currentUserService;

    public ForensicCorrelationService(
            ThreatIndicatorRepository indicatorRepository,
            CurrentUserService currentUserService) {

        this.indicatorRepository = indicatorRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ForensicCorrelationResponse> analyzeCorrelations() {

        Long currentOrganizationId =
                currentUserService.getCurrentOrganizationId();

        // Retrieve indicators belonging to ACTIVE organizations.
        List<ThreatIndicator> indicators =
                indicatorRepository
                        .findAllByThreat_Organization_StatusOrderByCreatedAtDesc(
                                "ACTIVE"
                        );

        // Only use indicators belonging to explicitly shared threats.
        Map<String, List<ThreatIndicator>> groupedIndicators =
                indicators.stream()
                        .filter(this::isEligibleForCorrelation)
                        .collect(Collectors.groupingBy(
                                this::buildMatchKey,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<ForensicCorrelationResponse> results =
                new ArrayList<>();

        for (List<ThreatIndicator> matches : groupedIndicators.values()) {

            // A correlation must involve at least two organizations.
            long distinctOrganizations = matches.stream()
                    .map(i -> i.getThreat()
                            .getOrganization().getId())
                    .distinct()
                    .count();

            if (distinctOrganizations < 2) {
                continue;
            }

            // Only return correlations involving the logged-in organization.
            boolean involvesCurrentOrganization = matches.stream()
                    .anyMatch(i -> i.getThreat()
                            .getOrganization().getId()
                            .equals(currentOrganizationId));

            if (!involvesCurrentOrganization) {
                continue;
            }

            results.add(buildResponse(matches));
        }

        return results.stream()
                .sorted(Comparator.comparingInt(
                        ForensicCorrelationResponse::evidenceScore
                ).reversed())
                .toList();
    }

    private boolean isEligibleForCorrelation(ThreatIndicator indicator) {

        Threat threat = indicator.getThreat();

        if (threat == null || threat.getOrganization() == null) {
            return false;
        }

        if (!"SHARED".equalsIgnoreCase(threat.getSharingStatus())) {
            return false;
        }

        if (indicator.getIndicatorType() == null
                || indicator.getIndicatorValue() == null) {
            return false;
        }

        return !indicator.getIndicatorValue().isBlank();
    }

    private String buildMatchKey(ThreatIndicator indicator) {

        String type = indicator.getIndicatorType()
                .trim()
                .toUpperCase(Locale.ROOT);

        String value = normalizeValue(
                indicator.getIndicatorValue()
        );

        return type + "::" + value;
    }

    private String normalizeValue(String value) {

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private ForensicCorrelationResponse buildResponse(
            List<ThreatIndicator> matches) {

        ThreatIndicator first = matches.get(0);

        List<IncidentEvidence> incidents = matches.stream()
                .map(this::toIncidentEvidence)
                .toList();

        int evidenceScore = calculateEvidenceScore(matches);

        String confidenceLabel;

        if (evidenceScore >= 80) {
            confidenceLabel = "HIGH";
        } else if (evidenceScore >= 50) {
            confidenceLabel = "MEDIUM";
        } else {
            confidenceLabel = "LOW";
        }

        return new ForensicCorrelationResponse(
                first.getIndicatorType(),
                first.getIndicatorValue(),
                evidenceScore,
                confidenceLabel,
                "Matching shared indicator found across "
                        + matches.stream()
                                .map(i -> i.getThreat()
                                        .getOrganization().getId())
                                .distinct()
                                .count()
                        + " organizations. This indicates a possible "
                        + "relationship, not confirmed threat attribution.",
                incidents
        );
    }

    private IncidentEvidence toIncidentEvidence(
            ThreatIndicator indicator) {

        Threat threat = indicator.getThreat();

        BigDecimal confidence = indicator.getConfidenceLevel();

        return new IncidentEvidence(
                threat.getId(),
                threat.getOrganization().getId(),
                threat.getCategory(),
                threat.getSeverity(),
                threat.getStatus(),
                confidence,
                indicator.getFirstObserved(),
                indicator.getLastObserved()
        );
    }

    private int calculateEvidenceScore(
            List<ThreatIndicator> matches) {

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (ThreatIndicator indicator : matches) {

            BigDecimal confidence = indicator.getConfidenceLevel();

            // Missing confidence is treated as unknown, not zero.
            if (confidence == null) {
                confidence = BigDecimal.valueOf(50);
            }

            confidence = confidence.max(BigDecimal.ZERO)
                    .min(BigDecimal.valueOf(100));

            total = total.add(confidence);
            count++;
        }

        if (count == 0) {
            return 0;
        }

        return total.divide(
                BigDecimal.valueOf(count),
                0,
                RoundingMode.HALF_UP
        ).intValue();
    }
}
