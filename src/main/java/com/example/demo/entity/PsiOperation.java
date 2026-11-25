
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
@Table(name = "psi_operations")
public class PsiOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cryptographic_operation_id", nullable = false)
    private CryptographicOperation cryptographicOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_organization_id", nullable = false)
    private Organization requestingOrganization;

    @Column(name = "protocol_name", nullable = false, length = 100)
    private String protocolName;

    @Column(name = "protocol_version", length = 50)
    private String protocolVersion;

    @Column(name = "purpose", nullable = false, columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "dataset_commitment", length = 128)
    private String datasetCommitment;

    @Column(name = "protocol_parameters", columnDefinition = "JSON")
    private String protocolParameters;

    @Column(name = "participant_count", nullable = false)
    private Integer participantCount = 2;

    @Column(name = "result_commitment", length = 128)
    private String resultCommitment;

    @Column(name = "result_reference", length = 512)
    private String resultReference;

    @Column(name = "psi_status", nullable = false, length = 30)
    private String psiStatus = "PENDING";

    @Column(name = "started_at")
    private LocalDateTime startedAt;

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

    public CryptographicOperation getCryptographicOperation() {
        return cryptographicOperation;
    }

    public void setCryptographicOperation(CryptographicOperation cryptographicOperation) {
        this.cryptographicOperation = cryptographicOperation;
    }

    public Organization getRequestingOrganization() {
        return requestingOrganization;
    }

    public void setRequestingOrganization(Organization requestingOrganization) {
        this.requestingOrganization = requestingOrganization;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDatasetCommitment() {
        return datasetCommitment;
    }

    public void setDatasetCommitment(String datasetCommitment) {
        this.datasetCommitment = datasetCommitment;
    }

    public String getProtocolParameters() {
        return protocolParameters;
    }

    public void setProtocolParameters(String protocolParameters) {
        this.protocolParameters = protocolParameters;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public String getResultCommitment() {
        return resultCommitment;
    }

    public void setResultCommitment(String resultCommitment) {
        this.resultCommitment = resultCommitment;
    }

    public String getResultReference() {
        return resultReference;
    }

    public void setResultReference(String resultReference) {
        this.resultReference = resultReference;
    }

    public String getPsiStatus() {
        return psiStatus;
    }

    public void setPsiStatus(String psiStatus) {
        this.psiStatus = psiStatus;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
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
