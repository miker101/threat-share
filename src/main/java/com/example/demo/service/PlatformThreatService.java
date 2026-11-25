
package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.PlatformThreatResponse;
import com.example.demo.dto.ThreatIndicatorResponse;
import com.example.demo.entity.Threat;
import com.example.demo.entity.ThreatIndicator;
import com.example.demo.repository.ThreatRepository;
import com.example.demo.repository.ThreatIndicatorRepository;

@Service
public class PlatformThreatService {

    private final ThreatRepository threatRepository;
    private final ThreatIndicatorRepository threatIndicatorRepository;

    public PlatformThreatService(
            ThreatRepository threatRepository,
            ThreatIndicatorRepository threatIndicatorRepository) {

        this.threatRepository = threatRepository;
        this.threatIndicatorRepository = threatIndicatorRepository;
    }

    @Transactional(readOnly = true)
    public List<PlatformThreatResponse> getAllPlatformThreats() {

        List<Threat> threats =
                threatRepository.findAllByOrderByCreatedAtDesc();

        return threats.stream()
                .map(threat -> {

                	List<ThreatIndicator> indicators =
                	        threatIndicatorRepository
                	                .findAllByThreat_IdAndThreat_Organization_IdOrderByCreatedAtDesc(
                	                        threat.getId(),
                	                        threat.getOrganization().getId()
                	                );

                    List<ThreatIndicatorResponse> indicatorResponses =
                            indicators.stream()
                                    .map(indicator -> new ThreatIndicatorResponse(
                                            indicator.getId(),
                                            indicator.getIndicatorType(),
                                            indicator.getIndicatorValue(),
                                            indicator.getDescription(),
                                            indicator.getConfidenceLevel(),
                                            indicator.getFirstObserved(),
                                            indicator.getLastObserved(),
                                            indicator.getCreatedAt()
                                    ))
                                    .toList();

                    return new PlatformThreatResponse(
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
                            threat.getUpdatedAt(),
                            threat.getOrganization().getId(),
                            threat.getOrganization().getName(),
                            threat.getSubmittedBy().getId(),
                            indicatorResponses
                    );
                })
                .toList();
    }
}
