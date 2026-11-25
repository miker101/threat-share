
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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "shared_intelligence_versions",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"shared_intelligence_id", "version_number"}
        )
    }
)
public class SharedIntelligenceVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_intelligence_id", nullable = false)
    private SharedIntelligence sharedIntelligence;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "sanitized_payload", nullable = false, columnDefinition = "JSON")
    private String sanitizedPayload;

    @Column(name = "content_hash", nullable = false, length = 128)
    private String contentHash;

    @Column(name = "hash_algorithm", nullable = false, length = 40)
    private String hashAlgorithm = "SHA-256";

    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SharedIntelligence getSharedIntelligence() {
        return sharedIntelligence;
    }

    public void setSharedIntelligence(SharedIntelligence sharedIntelligence) {
        this.sharedIntelligence = sharedIntelligence;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getSanitizedPayload() {
        return sanitizedPayload;
    }

    public void setSanitizedPayload(String sanitizedPayload) {
        this.sanitizedPayload = sanitizedPayload;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
