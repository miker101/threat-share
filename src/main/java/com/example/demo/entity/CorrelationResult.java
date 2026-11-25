package com.example.demo.entity;

import java.math.BigDecimal;
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
@Table(name = "correlation_results")
public class CorrelationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correlation_operation_id", nullable = false)
    private CorrelationOperation correlationOperation;

    @Column(name = "result_type", nullable = false, length = 60)
    private String resultType;

    @Column(name = "result_title", nullable = false, length = 200)
    private String resultTitle;

    @Column(name = "result_description", columnDefinition = "TEXT")
    private String resultDescription;

    @Column(name = "matching_indicator_count", nullable = false)
    private Integer matchingIndicatorCount = 0;

    @Column(name = "participating_organization_count", nullable = false)
    private Integer participatingOrganizationCount = 0;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "findings", columnDefinition = "JSON")
    private String findings;

    @Column(name = "result_reference", length = 512)
    private String resultReference;

    @Column(name = "result_hash", length = 128)
    private String resultHash;

    @Column(name = "hash_algorithm", nullable = false, length = 40)
    private String hashAlgorithm = "SHA-256";

    @Column(name = "access_policy", nullable = false, length = 40)
    private String accessPolicy = "PARTICIPANTS_ONLY";

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

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getResultTitle() {
        return resultTitle;
    }

    public void setResultTitle(String resultTitle) {
        this.resultTitle = resultTitle;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    public Integer getMatchingIndicatorCount() {
        return matchingIndicatorCount;
    }

    public void setMatchingIndicatorCount(Integer matchingIndicatorCount) {
        this.matchingIndicatorCount = matchingIndicatorCount;
    }

    public Integer getParticipatingOrganizationCount() {
        return participatingOrganizationCount;
    }

    public void setParticipatingOrganizationCount(Integer participatingOrganizationCount) {
        this.participatingOrganizationCount = participatingOrganizationCount;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public String getResultReference() {
        return resultReference;
    }

    public void setResultReference(String resultReference) {
        this.resultReference = resultReference;
    }

    public String getResultHash() {
        return resultHash;
    }

    public void setResultHash(String resultHash) {
        this.resultHash = resultHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getAccessPolicy() {
        return accessPolicy;
    }

    public void setAccessPolicy(String accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}