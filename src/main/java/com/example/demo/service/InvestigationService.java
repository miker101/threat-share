
package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.InvestigationResponse;
import com.example.demo.entity.Investigation;
import com.example.demo.entity.InvestigationThreat;
import com.example.demo.repository.InvestigationRepository;
import com.example.demo.security.CurrentUserService;

import com.example.demo.dto.InvestigationStatisticsResponse;

import com.example.demo.dto.InvestigationCreateRequest;
import com.example.demo.entity.Organization;
import com.example.demo.entity.User;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.UserRepository;

import com.example.demo.dto.InvestigationThreatResponse;
import com.example.demo.repository.InvestigationThreatRepository;

import com.example.demo.dto.InvestigationThreatLinkRequest;
import com.example.demo.repository.ThreatRepository;

import java.time.LocalDateTime;
import com.example.demo.entity.Evidence;
import com.example.demo.repository.EvidenceRepository;
@Service
public class InvestigationService {

    private final InvestigationRepository investigationRepository;
    private final CurrentUserService currentUserService;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final InvestigationThreatRepository investigationThreatRepository;
    private final ThreatRepository threatRepository;
    private final EvidenceRepository evidenceRepository;




    public InvestigationService(
            InvestigationRepository investigationRepository,
            CurrentUserService currentUserService,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            InvestigationThreatRepository investigationThreatRepository,
            ThreatRepository threatRepository,
            EvidenceRepository evidenceRepository) {

        this.investigationRepository = investigationRepository;
        this.currentUserService = currentUserService;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.investigationThreatRepository = investigationThreatRepository;
        this.threatRepository = threatRepository;
        this.evidenceRepository = evidenceRepository;
    }


    // Retrieve all investigations belonging to the authenticated organization.
    @Transactional(readOnly = true)
    public List<InvestigationResponse> getOrganizationInvestigations() {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        return investigationRepository
                .findAllByOrganization_IdOrderByCreatedAtDesc(organizationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieve one investigation while enforcing organization ownership.
    @Transactional(readOnly = true)
    public InvestigationResponse getInvestigation(Long investigationId) {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        Investigation investigation = investigationRepository
                .findByIdAndOrganization_Id(investigationId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Investigation not found."
                ));

        return mapToResponse(investigation);
    }

    // Retrieve investigation statistics for the authenticated organization.
    @Transactional(readOnly = true)
    public InvestigationStatisticsResponse getInvestigationStatistics() {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        long total = investigationRepository
                .countByOrganization_Id(organizationId);

        // OPEN and ACTIVE both represent investigations in progress.
        long active =
                investigationRepository.countByOrganization_IdAndStatus(
                        organizationId, "OPEN")
                + investigationRepository.countByOrganization_IdAndStatus(
                        organizationId, "ACTIVE");

        long underReview =
                investigationRepository.countByOrganization_IdAndStatus(
                        organizationId, "UNDER_REVIEW");

        // CLOSED and COMPLETED are both treated as completed.
        long completed =
                investigationRepository.countByOrganization_IdAndStatus(
                        organizationId, "COMPLETED")
                + investigationRepository.countByOrganization_IdAndStatus(
                        organizationId, "CLOSED");

        return new InvestigationStatisticsResponse(
                total,
                active,
                underReview,
                completed
        );
    }


    // Convert the entity into a safe API response.
    private InvestigationResponse mapToResponse(Investigation investigation) {

        String reference = String.format(
                "INV-%06d",
                investigation.getId()
        );

        return new InvestigationResponse(
                investigation.getId(),
                reference,
                investigation.getTitle(),
                investigation.getDescription(),
                investigation.getInvestigationType(),
                investigation.getPriority(),
                investigation.getStatus(),
                investigation.getLeadInvestigator(),
                investigation.getStartedAt(),
                investigation.getClosedAt(),
                investigation.getCreatedAt(),
                investigation.getUpdatedAt()
        );
    }

    @Transactional
    public InvestigationResponse createInvestigation(
            InvestigationCreateRequest request) {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        Long userId =
                currentUserService.getCurrentUserId();

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Organization not found."
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Current user not found."
                ));

        Investigation investigation = new Investigation();

        investigation.setOrganization(organization);
        investigation.setCreatedBy(user);
        investigation.setTitle(request.title().trim());
        investigation.setDescription(request.description());
        investigation.setInvestigationType(
                request.investigationType().trim()
        );

        String priority = request.priority();

        investigation.setPriority(
                priority == null || priority.isBlank()
                        ? "MEDIUM"
                        : priority.trim().toUpperCase()
        );

        investigation.setStatus("OPEN");
        investigation.setLeadInvestigator(request.leadInvestigator());

        Investigation saved =
                investigationRepository.saveAndFlush(investigation);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvestigationThreatResponse> getInvestigationThreats(Long investigationId) {

        getInvestigation(investigationId);

        return investigationThreatRepository
                .findAllByInvestigation_IdOrderByLinkedAtDesc(investigationId)
                .stream()
                .map(link -> {
                    var threat = link.getThreat();

                    return new InvestigationThreatResponse(
                            threat.getId(),
                            threat.getTitle(),
                            threat.getCategory(),
                            threat.getSeverity(),
                            threat.getStatus(),
                            link.getRelationshipType(),
                            link.getNotes(),
                            link.getLinkedAt()
                    );
                })
                .toList();
    }
    

@Transactional
public InvestigationThreatResponse linkThreatToInvestigation(
        Long investigationId,
        InvestigationThreatLinkRequest request) {

    Long organizationId = currentUserService.getCurrentOrganizationId();
    Long userId = currentUserService.getCurrentUserId();

    // Verify investigation ownership.
    getInvestigation(investigationId);

    // Only allow threats belonging to this organization.
    var threat = threatRepository
            .findByIdAndOrganization_Id(request.threatId(), organizationId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Threat not found in your organization."
            ));

    // Prevent duplicate links.
    if (investigationThreatRepository.existsByInvestigation_IdAndThreat_Id(
            investigationId, request.threatId())) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This threat is already linked to the investigation."
        );
    }

    User linkedBy = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Current user not found."
            ));

    Investigation investigation = investigationRepository
            .findByIdAndOrganization_Id(investigationId, organizationId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Investigation not found."
            ));

    InvestigationThreat link = new InvestigationThreat();
    link.setInvestigation(investigation);
    link.setThreat(threat);
    link.setLinkedBy(linkedBy);

    String relationshipType = request.relationshipType();

    link.setRelationshipType(
            relationshipType == null || relationshipType.isBlank()
                    ? "RELATED"
                    : relationshipType.trim().toUpperCase()
    );

    link.setNotes(request.notes());

    InvestigationThreat saved =
            investigationThreatRepository.saveAndFlush(link);

    return new InvestigationThreatResponse(
            threat.getId(),
            threat.getTitle(),
            threat.getCategory(),
            threat.getSeverity(),
            threat.getStatus(),
            saved.getRelationshipType(),
            saved.getNotes(),
            saved.getLinkedAt()
    );
}
public record EvidenceResponse(
        Long id,
        String evidenceType,
        String title,
        String description,
        String evidenceHash,
        String hashAlgorithm,
        Long fileSize,
        String originalFilename,
        LocalDateTime collectedAt,
        LocalDateTime createdAt
) {}

@Transactional(readOnly = true)
public List<EvidenceResponse> getInvestigationEvidence(Long investigationId) {

    // Enforce authenticated organization's investigation access.
    getInvestigation(investigationId);

    return evidenceRepository
            .findAllByInvestigation_IdOrderByCollectedAtDesc(investigationId)
            .stream()
            .map(evidence -> new EvidenceResponse(
                    evidence.getId(),
                    evidence.getEvidenceType(),
                    evidence.getTitle(),
                    evidence.getDescription(),
                    evidence.getEvidenceHash(),
                    evidence.getHashAlgorithm(),
                    evidence.getFileSize(),
                    evidence.getOriginalFilename(),
                    evidence.getCollectedAt(),
                    evidence.getCreatedAt()
            ))
            .toList();
}


}
