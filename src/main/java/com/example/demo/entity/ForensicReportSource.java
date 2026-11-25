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
@Table(name = "forensic_report_sources")
public class ForensicReportSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forensic_report_id", nullable = false)
    private ForensicReport forensicReport;

    @Column(name = "source_type", nullable = false, length = 80)
    private String sourceType;

    @Column(name = "source_record_id", nullable = false)
    private Long sourceRecordId;

    @Column(name = "source_description", columnDefinition = "TEXT")
    private String sourceDescription;

    @Column(name = "source_hash", length = 128)
    private String sourceHash;

    @Column(name = "relevance", nullable = false, length = 40)
    private String relevance = "SUPPORTING";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "included_by", nullable = false)
    private User includedBy;

    @Column(name = "included_at", insertable = false, updatable = false)
    private LocalDateTime includedAt;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ForensicReport getForensicReport() {
        return forensicReport;
    }

    public void setForensicReport(ForensicReport forensicReport) {
        this.forensicReport = forensicReport;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceRecordId() {
        return sourceRecordId;
    }

    public void setSourceRecordId(Long sourceRecordId) {
        this.sourceRecordId = sourceRecordId;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public void setSourceDescription(String sourceDescription) {
        this.sourceDescription = sourceDescription;
    }

    public String getSourceHash() {
        return sourceHash;
    }

    public void setSourceHash(String sourceHash) {
        this.sourceHash = sourceHash;
    }

    public String getRelevance() {
        return relevance;
    }

    public void setRelevance(String relevance) {
        this.relevance = relevance;
    }

    public User getIncludedBy() {
        return includedBy;
    }

    public void setIncludedBy(User includedBy) {
        this.includedBy = includedBy;
    }

    public LocalDateTime getIncludedAt() {
        return includedAt;
    }
}