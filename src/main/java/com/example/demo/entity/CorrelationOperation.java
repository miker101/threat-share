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
@Table(name = "correlation_operations")
public class CorrelationOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiated_by_organization_id", nullable = false)
    private Organization initiatedByOrganization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiated_by", nullable = false)
    private User initiatedBy;

    @Column(name = "operation_name", nullable = false, length = 200)
    private String operationName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "correlation_type", nullable = false, length = 60)
    private String correlationType;

    @Column(name = "correlation_method", nullable = false, length = 100)
    private String correlationMethod;

    @Column(name = "matching_parameters", columnDefinition = "JSON")
    private String matchingParameters;

    @Column(name = "participant_count", nullable = false)
    private Integer participantCount = 2;

    @Column(name = "operation_status", nullable = false, length = 30)
    private String operationStatus = "PENDING";

    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    @Column(name = "result_reference", length = 512)
    private String resultReference;

    @Column(name = "result_hash", length = 128)
    private String resultHash;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

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

    public Organization getInitiatedByOrganization() {
        return initiatedByOrganization;
    }

    public void setInitiatedByOrganization(Organization initiatedByOrganization) {
        this.initiatedByOrganization = initiatedByOrganization;
    }

    public User getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(User initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCorrelationType() {
        return correlationType;
    }

    public void setCorrelationType(String correlationType) {
        this.correlationType = correlationType;
    }

    public String getCorrelationMethod() {
        return correlationMethod;
    }

    public void setCorrelationMethod(String correlationMethod) {
        this.correlationMethod = correlationMethod;
    }

    public String getMatchingParameters() {
        return matchingParameters;
    }

    public void setMatchingParameters(String matchingParameters) {
        this.matchingParameters = matchingParameters;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
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
}