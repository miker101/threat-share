
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
@Table(name = "threat_actor_associations")
public class ThreatActorAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threat_actor_id", nullable = false)
    private ThreatActor threatActor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threat_id")
    private Threat threat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigation_id")
    private Investigation investigation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correlation_result_id")
    private CorrelationResult correlationResult;

    @Column(name = "association_type", nullable = false, length = 80)
    private String associationType;

    @Column(name = "association_description", columnDefinition = "TEXT")
    private String associationDescription;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "attribution_status", nullable = false, length = 40)
    private String attributionStatus = "SUSPECTED";

    @Column(name = "supporting_reference", length = 512)
    private String supportingReference;

    @Column(name = "supporting_hash", length = 128)
    private String supportingHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identified_by", nullable = false)
    private User identifiedBy;

    @Column(name = "identified_at", insertable = false, updatable = false)
    private LocalDateTime identifiedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ThreatActor getThreatActor() {
        return threatActor;
    }

    public void setThreatActor(ThreatActor threatActor) {
        this.threatActor = threatActor;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Threat getThreat() {
        return threat;
    }

    public void setThreat(Threat threat) {
        this.threat = threat;
    }

    public Investigation getInvestigation() {
        return investigation;
    }

    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
    }

    public CorrelationResult getCorrelationResult() {
        return correlationResult;
    }

    public void setCorrelationResult(CorrelationResult correlationResult) {
        this.correlationResult = correlationResult;
    }

    public String getAssociationType() {
        return associationType;
    }

    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    public String getAssociationDescription() {
        return associationDescription;
    }

    public void setAssociationDescription(String associationDescription) {
        this.associationDescription = associationDescription;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getAttributionStatus() {
        return attributionStatus;
    }

    public void setAttributionStatus(String attributionStatus) {
        this.attributionStatus = attributionStatus;
    }

    public String getSupportingReference() {
        return supportingReference;
    }

    public void setSupportingReference(String supportingReference) {
        this.supportingReference = supportingReference;
    }

    public String getSupportingHash() {
        return supportingHash;
    }

    public void setSupportingHash(String supportingHash) {
        this.supportingHash = supportingHash;
    }

    public User getIdentifiedBy() {
        return identifiedBy;
    }

    public void setIdentifiedBy(User identifiedBy) {
        this.identifiedBy = identifiedBy;
    }

    public LocalDateTime getIdentifiedAt() {
        return identifiedAt;
    }
}
