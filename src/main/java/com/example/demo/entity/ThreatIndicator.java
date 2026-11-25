package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "threat_indicators")
public class ThreatIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "threat_id", nullable = false)
    private Threat threat;

    @Column(name = "indicator_type", nullable = false, length = 40)
    private String indicatorType;

    @Column(name = "indicator_value", nullable = false, length = 512)
    private String indicatorValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "confidence_level", precision = 5, scale = 2)
    private BigDecimal confidenceLevel;

    @Column(name = "first_observed")
    private LocalDateTime firstObserved;

    @Column(name = "last_observed")
    private LocalDateTime lastObserved;

    // Generated and managed by MySQL
    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ThreatIndicator() {
    }

    public Long getId() {
        return id;
    }

    public Threat getThreat() {
        return threat;
    }

    public void setThreat(Threat threat) {
        this.threat = threat;
    }

    public String getIndicatorType() {
        return indicatorType;
    }

    public void setIndicatorType(String indicatorType) {
        this.indicatorType = indicatorType;
    }

    public String getIndicatorValue() {
        return indicatorValue;
    }

    public void setIndicatorValue(String indicatorValue) {
        this.indicatorValue = indicatorValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(BigDecimal confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}