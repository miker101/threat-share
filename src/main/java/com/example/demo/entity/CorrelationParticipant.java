package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "correlation_participants",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_correlation_operation_organization",
            columnNames = {"correlation_operation_id", "organization_id"}
        )
    }
)
public class CorrelationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correlation_operation_id", nullable = false)
    private CorrelationOperation correlationOperation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_user_id")
    private User participantUser;

    @Column(name = "participant_role", nullable = false, length = 40)
    private String participantRole = "PARTICIPANT";

    @Column(name = "participation_status", nullable = false, length = 30)
    private String participationStatus = "INVITED";

    @Column(name = "consent_status", nullable = false, length = 30)
    private String consentStatus = "PENDING";

    @Column(name = "consented_at")
    private LocalDateTime consentedAt;

    @Column(name = "contribution_reference", length = 512)
    private String contributionReference;

    @Column(name = "contribution_hash", length = 128)
    private String contributionHash;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CorrelationOperation getCorrelationOperation() {
        return correlationOperation;
    }

    public void setCorrelationOperation(CorrelationOperation correlationOperation) {
        this.correlationOperation = correlationOperation;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getParticipantUser() {
        return participantUser;
    }

    public void setParticipantUser(User participantUser) {
        this.participantUser = participantUser;
    }

    public String getParticipantRole() {
        return participantRole;
    }

    public void setParticipantRole(String participantRole) {
        this.participantRole = participantRole;
    }

    public String getParticipationStatus() {
        return participationStatus;
    }

    public void setParticipationStatus(String participationStatus) {
        this.participationStatus = participationStatus;
    }

    public String getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(String consentStatus) {
        this.consentStatus = consentStatus;
    }

    public LocalDateTime getConsentedAt() {
        return consentedAt;
    }

    public void setConsentedAt(LocalDateTime consentedAt) {
        this.consentedAt = consentedAt;
    }

    public String getContributionReference() {
        return contributionReference;
    }

    public void setContributionReference(String contributionReference) {
        this.contributionReference = contributionReference;
    }

    public String getContributionHash() {
        return contributionHash;
    }

    public void setContributionHash(String contributionHash) {
        this.contributionHash = contributionHash;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}