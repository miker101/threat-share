
package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "threats")
public class Threat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The organization that owns this threat.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // The organization user who submitted this threat.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 60)
    private String category;

    @Column(name = "severity", nullable = false, length = 30)
    private String severity;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "OPEN";

    @Column(name = "sharing_status", nullable = false, length = 30)
    private String sharingStatus = "PRIVATE";

    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "first_observed")
    private LocalDateTime firstObserved;

    @Column(name = "last_observed")
    private LocalDateTime lastObserved;

    // Managed by MySQL.
    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Managed by MySQL, including ON UPDATE CURRENT_TIMESTAMP(6).
    @Column(name = "updated_at", nullable = false,
            insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Threat() {}

    public Long getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSharingStatus() {
        return sharingStatus;
    }

    public void setSharingStatus(String sharingStatus) {
        this.sharingStatus = sharingStatus;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
