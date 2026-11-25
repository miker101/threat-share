
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CryptographicOperation;
import com.example.demo.entity.Organization;
import com.example.demo.entity.PsiOperation;
import com.example.demo.entity.PsiOperationParticipant;
import com.example.demo.entity.User;
import com.example.demo.repository.CryptographicOperationRepository;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.PsiOperationParticipantRepository;
import com.example.demo.repository.PsiOperationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CurrentUserService;




@Service
public class PsiOperationService {

    private static final String PROTOCOL_NAME =
            "Two-Party Elliptic-Curve PSI";

    private static final String PROTOCOL_VERSION = "1.0";

    private static final String ALGORITHM =
            "P-256";

    private final PsiOperationRepository psiOperationRepository;
   
    private final CryptographicOperationRepository cryptographicOperationRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PsiOperationParticipantRepository psiOperationParticipantRepository;

    public PsiOperationService(
            PsiOperationRepository psiOperationRepository,
            PsiOperationParticipantRepository psiOperationParticipantRepository,
            CryptographicOperationRepository cryptographicOperationRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService) {

        this.psiOperationRepository = psiOperationRepository;
        this.psiOperationParticipantRepository = psiOperationParticipantRepository;
        this.cryptographicOperationRepository = cryptographicOperationRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Creates a PSI operation.
     *
     * All ACTIVE organizations become eligible automatically.
     * Only the requesting organization is initially JOINED.
     *
     * No threat indicator values or cryptographic secrets
     * are loaded or shared by this method.
     */
    @Transactional
    public PsiOperation createOperation(String purpose) {

        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException(
                    "PSI operation purpose is required.");
        }

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        Long userId =
                currentUserService.getCurrentUserId();

        Organization requestingOrganization =
                organizationRepository.findById(organizationId)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Requesting organization not found."));

        if (!"ACTIVE".equalsIgnoreCase(
                requestingOrganization.getStatus())) {

            throw new AccessDeniedException(
                    "Only ACTIVE organizations can initiate PSI operations.");
        }

        User initiatingUser =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Authenticated user not found."));

        // Find all approved organizations automatically.
        List<Organization> activeOrganizations =
                organizationRepository
                        .findByStatusOrderByCreatedAtAsc("ACTIVE");

        if (activeOrganizations.size() < 2) {
            throw new IllegalStateException(
                    "PSI requires at least two ACTIVE organizations.");
        }

        // Create the general cryptographic operation record.
        CryptographicOperation cryptographicOperation =
                new CryptographicOperation();

        cryptographicOperation.setOrganization(
                requestingOrganization);

        cryptographicOperation.setInitiatedBy(initiatingUser);

        cryptographicOperation.setOperationType("PSI");

        cryptographicOperation.setOperationReference(
                "PSI-" + UUID.randomUUID());

        cryptographicOperation.setAlgorithm(ALGORITHM);

        cryptographicOperation.setOperationStatus("PENDING");

        cryptographicOperation =
                cryptographicOperationRepository.save(
                        cryptographicOperation);

        // Create the PSI-specific operation record.
        PsiOperation psiOperation = new PsiOperation();

        psiOperation.setCryptographicOperation(
                cryptographicOperation);

        psiOperation.setRequestingOrganization(
                requestingOrganization);

        psiOperation.setProtocolName(PROTOCOL_NAME);

        psiOperation.setProtocolVersion(PROTOCOL_VERSION);

        psiOperation.setPurpose(purpose.trim());

     // The initiating organization is the first joined participant.
        psiOperation.setParticipantCount(1);

        psiOperation.setPsiStatus("PENDING");

        psiOperation.setProtocolParameters(
                "{\"curve\":\"secp256r1\","
                + "\"hashSuite\":\"P256_XMD:SHA-256_SSWU_RO_\"}");

        psiOperation =
                psiOperationRepository.save(psiOperation);

        // Register eligible organizations for this operation.
        List<PsiOperationParticipant> participants =
                new ArrayList<>();

        for (Organization organization : activeOrganizations) {

            PsiOperationParticipant participant =
                    new PsiOperationParticipant();

            participant.setPsiOperation(psiOperation);

            participant.setOrganization(organization);

            if (organization.getId().equals(organizationId)) {

                participant.setParticipantUser(initiatingUser);

                participant.setParticipantRole("INITIATOR");

                participant.setParticipationStatus("JOINED");

                participant.setJoinedAt(LocalDateTime.now());

            } else {

                participant.setParticipantRole("PARTICIPANT");

                participant.setParticipationStatus("ELIGIBLE");
            }

            participants.add(participant);
        }

        psiOperationParticipantRepository.saveAll(participants);

        return psiOperation;
    }

    /**
     * Retrieves PSI operations initiated by the authenticated
     * organization only.
     */
    @Transactional(readOnly = true)
    public List<PsiOperation> getMyOperations() {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        return psiOperationRepository
                .findAllByRequestingOrganization_IdOrderByCreatedAtDesc(
                        organizationId);
    }
    

@Transactional(readOnly = true)
public List<PsiOperationParticipant> getParticipants(Long operationId) {

    Long organizationId = currentUserService.getCurrentOrganizationId();

    PsiOperation operation = psiOperationRepository.findById(operationId)
            .orElseThrow(() ->
                    new IllegalArgumentException("PSI operation not found"));

    // Only organizations registered for this operation can view its participants.
    psiOperationParticipantRepository
            .findByPsiOperation_IdAndOrganization_Id(operationId, organizationId)
            .orElseThrow(() ->
                    new org.springframework.security.access.AccessDeniedException(
                            "You are not authorized to view this PSI operation."));

    return psiOperationParticipantRepository
            .findAllByPsiOperation_IdOrderByCreatedAtAsc(operation.getId());
}

@Transactional
public PsiOperationParticipant joinOperation(Long operationId) {

    Long organizationId = currentUserService.getCurrentOrganizationId();

    PsiOperation operation = psiOperationRepository.findById(operationId)
            .orElseThrow(() ->
                    new IllegalArgumentException("PSI operation not found"));

    if (!"PENDING".equalsIgnoreCase(operation.getPsiStatus())) {
        throw new IllegalStateException(
                "This PSI operation is no longer accepting participants.");
    }

    PsiOperationParticipant participant =
            psiOperationParticipantRepository
                    .findByPsiOperation_IdAndOrganization_Id(
                            operationId, organizationId)
                    .orElseThrow(() ->
                            new org.springframework.security.access.AccessDeniedException(
                                    "Your organization is not eligible for this operation."));

    if ("JOINED".equalsIgnoreCase(participant.getParticipationStatus())) {
        return participant;
    }

    if (!"ELIGIBLE".equalsIgnoreCase(participant.getParticipationStatus())) {
        throw new IllegalStateException(
                "Your organization cannot join in its current participation state.");
    }

    participant.setParticipationStatus("JOINED");
    participant.setJoinedAt(LocalDateTime.now());

    PsiOperationParticipant savedParticipant =
            psiOperationParticipantRepository.save(participant);

    // Update the actual number of joined organizations.
    long joinedCount =
            psiOperationParticipantRepository
                    .countByPsiOperation_IdAndParticipationStatus(
                            operationId, "JOINED");

    operation.setParticipantCount(Math.toIntExact(joinedCount));
    psiOperationRepository.save(operation);

    return savedParticipant;
}

@Transactional(readOnly = true)
public boolean isReadyForExecution(Long operationId) {

    Long organizationId =
            currentUserService.getCurrentOrganizationId();

    PsiOperation operation =
            psiOperationRepository.findById(operationId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "PSI operation not found."));

    // Only the initiating organization can check start readiness.
    if (!operation.getRequestingOrganization()
            .getId().equals(organizationId)) {
        throw new AccessDeniedException(
                "Only the initiating organization can check execution readiness.");
    }

    if (!"PENDING".equalsIgnoreCase(operation.getPsiStatus())) {
        return false;
    }

    long joinedCount =
            psiOperationParticipantRepository
                    .countByPsiOperation_IdAndParticipationStatus(
                            operationId, "JOINED");

    return joinedCount >= 2;
}


}
