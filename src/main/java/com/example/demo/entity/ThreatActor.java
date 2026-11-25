
package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "threat_actors")
public class ThreatActor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_name", nullable = false, length = 200)
    private String actorName;

    @Column(name = "primary_alias", length = 200)
    private String primaryAlias;

    @Column(name = "actor_type", nullable = false, length = 60)
    private String actorType;

    @Column(name = "classification", length = 60)
    private String classification;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "suspected_motivation", columnDefinition = "TEXT")
    private String suspectedMotivation;

    @Column(name = "origin_country", length = 100)
    private String originCountry;

    @Column(name = "sophistication_level", length = 40)
    private String sophisticationLevel;

    @Column(name = "attribution_confidence", precision = 5, scale = 2)
    private BigDecimal attributionConfidence;

    @Column(name = "attribution_status", nullable = false, length = 40)
    private String attributionStatus = "UNCONFIRMED";

    @Column(name = "first_observed")
    private LocalDateTime firstObserved;

    @Column(name = "last_observed")
    private LocalDateTime lastObserved;

    @Column(name = "profile_reference", length = 512)
    private String profileReference;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getPrimaryAlias() {
        return primaryAlias;
    }

    public void setPrimaryAlias(String primaryAlias) {
        this.primaryAlias = primaryAlias;
    }

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuspectedMotivation() {
        return suspectedMotivation;
    }

    public void setSuspectedMotivation(String suspectedMotivation) {
        this.suspectedMotivation = suspectedMotivation;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getSophisticationLevel() {
        return sophisticationLevel;
    }

    public void setSophisticationLevel(String sophisticationLevel) {
        this.sophisticationLevel = sophisticationLevel;
    }

    public BigDecimal getAttributionConfidence() {
        return attributionConfidence;
    }

    public void setAttributionConfidence(BigDecimal attributionConfidence) {
        this.attributionConfidence = attributionConfidence;
    }

    public String getAttributionStatus() {
        return attributionStatus;
    }

    public void setAttributionStatus(String attributionStatus) {
        this.attributionStatus = attributionStatus;
    }

    public LocalDateTime getFirstObserved() {
        return firstObserved;
    }

    public void setFirstObserved(LocalDateTime firstObserved) {
        this.firstObserved = firstObserved;
    }

    public LocalDateTime getLastObserved() {
        return lastObserved;
    }

    public void setLastObserved(LocalDateTime lastObserved) {
        this.lastObserved = lastObserved;
    }

    public String getProfileReference() {
        return profileReference;
    }

    public void setProfileReference(String profileReference) {
        this.profileReference = profileReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
