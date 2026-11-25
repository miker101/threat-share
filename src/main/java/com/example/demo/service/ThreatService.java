
package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ThreatCreateRequest;
import com.example.demo.dto.ThreatIndicatorRequest;
import com.example.demo.dto.ThreatIndicatorResponse;
import com.example.demo.dto.ThreatResponse;

import com.example.demo.entity.Organization;
import com.example.demo.entity.Threat;
import com.example.demo.entity.ThreatIndicator;
import com.example.demo.entity.User;

import com.example.demo.repository.ThreatRepository;
import com.example.demo.repository.ThreatIndicatorRepository;

import com.example.demo.security.CurrentUserService;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.ThreatUpdateRequest;

@Service
public class ThreatService {

    private final ThreatRepository threatRepository;
    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final EntityManager entityManager;

    public ThreatService(
            ThreatRepository threatRepository,
            ThreatIndicatorRepository threatIndicatorRepository,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            EntityManager entityManager) {

        this.threatRepository = threatRepository;
        this.threatIndicatorRepository = threatIndicatorRepository;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.entityManager = entityManager;
    }

    @Transactional
    public ThreatResponse createThreat(
            ThreatCreateRequest request,
            HttpServletRequest httpRequest) {

        // Obtain identity exclusively from the authenticated principal.
        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        Long userId =
                currentUserService.getCurrentUserId();

        // Associate the threat with its owning organization and submitter.
        Threat threat = new Threat();

        threat.setOrganization(
                entityManager.getReference(
                        Organization.class, organizationId));

        threat.setSubmittedBy(
                entityManager.getReference(
                        User.class, userId));

        threat.setTitle(request.title());
        threat.setDescription(request.description());
        threat.setCategory(request.category());
        threat.setSeverity(request.severity());
        threat.setSource(request.source());
        threat.setFirstObserved(request.firstObserved());
        threat.setLastObserved(request.lastObserved());

        // Persist the threat before creating its indicators.
        threat = threatRepository.saveAndFlush(threat);

        List<ThreatIndicator> savedIndicators = new ArrayList<>();

        if (request.indicators() != null) {

            for (ThreatIndicatorRequest indicatorRequest
                    : request.indicators()) {

                ThreatIndicator indicator = new ThreatIndicator();

                indicator.setThreat(threat);
                indicator.setIndicatorType(
                        indicatorRequest.indicatorType());
                indicator.setIndicatorValue(
                        indicatorRequest.indicatorValue());
                indicator.setDescription(
                        indicatorRequest.description());
                indicator.setConfidenceLevel(
                        indicatorRequest.confidenceLevel());
                indicator.setFirstObserved(
                        indicatorRequest.firstObserved());
                indicator.setLastObserved(
                        indicatorRequest.lastObserved());

                savedIndicators.add(indicator);
            }
        }

        if (!savedIndicators.isEmpty()) {
            savedIndicators =
                    threatIndicatorRepository.saveAllAndFlush(
                            savedIndicators);
        }

        // Refresh database-generated timestamps.
        entityManager.refresh(threat);

        for (ThreatIndicator indicator : savedIndicators) {
            entityManager.refresh(indicator);
        }

        // Record the successful submission in the protected audit chain.
        auditLogService.recordSuccess(
                "THREAT_CREATED",
                "THREAT_INTELLIGENCE",
                "THREAT",
                threat.getId().toString(),
                "Threat submitted: " + threat.getTitle()
                        + ". IOC count: " + savedIndicators.size(),
                organizationId,
                httpRequest
        );

        return mapToResponse(threat, savedIndicators);
    }

    private ThreatResponse mapToResponse(
            Threat threat,
            List<ThreatIndicator> indicators) {

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

        return new ThreatResponse(
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
                threat.getSubmittedBy().getId(),
                indicatorResponses
        );
    }

@Transactional
public ThreatResponse updateThreat(
        Long threatId,
        ThreatUpdateRequest request,
        HttpServletRequest httpRequest) {

    // Get the organization from the authenticated user.
    Long organizationId =
            currentUserService.getCurrentOrganizationId();

    // Only retrieve a threat owned by this organization.
    Threat threat = threatRepository
            .findByIdAndOrganization_Id(threatId, organizationId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Threat not found"));

    // Update the threat's editable fields.
    threat.setTitle(request.title());
    threat.setDescription(request.description());
    threat.setCategory(request.category());
    threat.setSeverity(request.severity());
    threat.setSource(request.source());
    threat.setFirstObserved(request.firstObserved());
    threat.setLastObserved(request.lastObserved());

    threat = threatRepository.saveAndFlush(threat);

    // Retrieve existing indicators belonging to this threat
    // and authenticated organization.
    List<ThreatIndicator> existingIndicators =
            threatIndicatorRepository
                    .findAllByThreat_IdAndThreat_Organization_IdOrderByCreatedAtDesc(
                            threatId, organizationId);

    // Delete existing indicators before inserting replacements.
    if (!existingIndicators.isEmpty()) {
        threatIndicatorRepository.deleteAll(existingIndicators);
        threatIndicatorRepository.flush();
    }

    // Build the replacement indicator list.
    List<ThreatIndicator> updatedIndicators = new ArrayList<>();

    for (ThreatIndicatorRequest indicatorRequest : request.indicators()) {

        ThreatIndicator indicator = new ThreatIndicator();

        indicator.setThreat(threat);
        indicator.setIndicatorType(indicatorRequest.indicatorType());
        indicator.setIndicatorValue(indicatorRequest.indicatorValue());
        indicator.setDescription(indicatorRequest.description());
        indicator.setConfidenceLevel(indicatorRequest.confidenceLevel());
        indicator.setFirstObserved(indicatorRequest.firstObserved());
        indicator.setLastObserved(indicatorRequest.lastObserved());

        updatedIndicators.add(indicator);
    }

    // Persist the replacement indicators.
    if (!updatedIndicators.isEmpty()) {
        updatedIndicators =
                threatIndicatorRepository.saveAllAndFlush(updatedIndicators);

        // Refresh database-generated timestamps.
        for (ThreatIndicator indicator : updatedIndicators) {
            entityManager.refresh(indicator);
        }
    }

    // Refresh the threat's database-managed timestamps.
    entityManager.refresh(threat);

    // Record the update in the audit chain.
    auditLogService.recordSuccess(
            "THREAT_UPDATED",
            "THREAT_INTELLIGENCE",
            "THREAT",
            threat.getId().toString(),
            "Threat updated: " + threat.getTitle()
                    + ". IOC count: " + updatedIndicators.size(),
            organizationId,
            httpRequest
    );

    return mapToResponse(threat, updatedIndicators);
}

    @Transactional(readOnly = true)
    public List<ThreatResponse> getOrganizationThreats() {

        Long organizationId = currentUserService.getCurrentOrganizationId();

        List<Threat> threats = threatRepository
                .findAllByOrganization_IdOrderByCreatedAtDesc(organizationId);

        return threats.stream()
                .map(threat -> {
                    List<ThreatIndicator> indicators =
                            threatIndicatorRepository
                                    .findAllByThreat_IdAndThreat_Organization_IdOrderByCreatedAtDesc(
                                            threat.getId(), organizationId);

                    return mapToResponse(threat, indicators);
                })
                .toList();
    }
    @Transactional(readOnly = true)
    public ThreatResponse getThreatById(Long threatId) {

        Long organizationId = currentUserService.getCurrentOrganizationId();

        Threat threat = threatRepository
                .findByIdAndOrganization_Id(threatId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Threat not found"));

        List<ThreatIndicator> indicators =
                threatIndicatorRepository
                        .findAllByThreat_IdAndThreat_Organization_IdOrderByCreatedAtDesc(
                                threatId, organizationId);

        return mapToResponse(threat, indicators);
    }
}
