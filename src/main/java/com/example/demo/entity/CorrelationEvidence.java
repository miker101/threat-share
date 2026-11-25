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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "correlation_evidence",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_correlation_result_evidence",
            columnNames = {"correlation_result_id", "evidence_id"}
        )
    }
)
public class CorrelationEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correlation_result_id", nullable = false)
    private CorrelationResult correlationResult;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_id", nullable = false)
    private Evidence evidence;

    @Column(name = "relationship_type", nullable = false, length = 60)
    private String relationshipType;

    @Column(name = "relevance_score", precision = 5, scale = 2)
    private BigDecimal relevanceScore;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linked_by", nullable = false)
    private User linkedBy;

    @Column(name = "linked_at", insertable = false, updatable = false)
    private LocalDateTime linkedAt;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CorrelationResult getCorrelationResult() {
        return correlationResult;
    }

    public void setCorrelationResult(CorrelationResult correlationResult) {
        this.correlationResult = correlationResult;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public BigDecimal getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(BigDecimal relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public User getLinkedBy() {
        return linkedBy;
    }

    public void setLinkedBy(User linkedBy) {
        this.linkedBy = linkedBy;
    }

    public LocalDateTime getLinkedAt() {
        return linkedAt;
    }
}