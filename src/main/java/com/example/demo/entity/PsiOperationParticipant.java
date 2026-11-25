
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

@Entity
@Table(name = "psi_operation_participants")
public class PsiOperationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psi_operation_id", nullable = false)
    private PsiOperation psiOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_user_id")
    private User participantUser;

    @Column(name = "participant_role", nullable = false, length = 30)
    private String participantRole = "PARTICIPANT";

    @Column(name = "participation_status", nullable = false, length = 30)
    private String participationStatus = "INVITED";

    @Column(name = "dataset_commitment", length = 128)
    private String datasetCommitment;

    @Column(name = "contribution_reference", length = 512)
    private String contributionReference;

    @Column(name = "result_reference", length = 512)
    private String resultReference;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PsiOperation getPsiOperation() {
        return psiOperation;
    }

    public void setPsiOperation(PsiOperation psiOperation) {
        this.psiOperation = psiOperation;
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

    public String getDatasetCommitment() {
        return datasetCommitment;
    }

    public void setDatasetCommitment(String datasetCommitment) {
        this.datasetCommitment = datasetCommitment;
    }

    public String getContributionReference() {
        return contributionReference;
    }

    public void setContributionReference(String contributionReference) {
        this.contributionReference = contributionReference;
    }

    public String getResultReference() {
        return resultReference;
    }

    public void setResultReference(String resultReference) {
        this.resultReference = resultReference;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
