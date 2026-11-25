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
@Table(name = "homomorphic_operations")
public class HomomorphicOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cryptographic_operation_id", nullable = false)
    private CryptographicOperation cryptographicOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "encryption_scheme", nullable = false, length = 100)
    private String encryptionScheme;

    @Column(name = "scheme_parameters", columnDefinition = "JSON")
    private String schemeParameters;

    @Column(name = "computation_type", nullable = false, length = 100)
    private String computationType;

    @Column(name = "input_ciphertext_reference", nullable = false, length = 512)
    private String inputCiphertextReference;

    @Column(name = "output_ciphertext_reference", length = 512)
    private String outputCiphertextReference;

    @Column(name = "evaluation_key_reference", length = 512)
    private String evaluationKeyReference;

    @Column(name = "input_commitment", length = 128)
    private String inputCommitment;

    @Column(name = "output_commitment", length = 128)
    private String outputCommitment;

    @Column(name = "operation_status", nullable = false, length = 30)
    private String operationStatus = "PENDING";

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

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getEncryptionScheme() {
        return encryptionScheme;
    }

    public void setEncryptionScheme(String encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
    }

    public String getSchemeParameters() {
        return schemeParameters;
    }

    public void setSchemeParameters(String schemeParameters) {
        this.schemeParameters = schemeParameters;
    }

    public String getComputationType() {
        return computationType;
    }

    public void setComputationType(String computationType) {
        this.computationType = computationType;
    }

    public String getInputCiphertextReference() {
        return inputCiphertextReference;
    }

    public void setInputCiphertextReference(String inputCiphertextReference) {
        this.inputCiphertextReference = inputCiphertextReference;
    }

    public String getOutputCiphertextReference() {
        return outputCiphertextReference;
    }

    public void setOutputCiphertextReference(String outputCiphertextReference) {
        this.outputCiphertextReference = outputCiphertextReference;
    }

    public String getEvaluationKeyReference() {
        return evaluationKeyReference;
    }

    public void setEvaluationKeyReference(String evaluationKeyReference) {
        this.evaluationKeyReference = evaluationKeyReference;
    }

    public String getInputCommitment() {
        return inputCommitment;
    }

    public void setInputCommitment(String inputCommitment) {
        this.inputCommitment = inputCommitment;
    }

    public String getOutputCommitment() {
        return outputCommitment;
    }

    public void setOutputCommitment(String outputCommitment) {
        this.outputCommitment = outputCommitment;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
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