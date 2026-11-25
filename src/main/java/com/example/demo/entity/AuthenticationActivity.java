
package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "authentication_activity",
    indexes = {
        @Index(name = "idx_auth_activity_type", columnList = "event_type"),
        @Index(name = "idx_auth_activity_admin", columnList = "administrator_type, administrator_id"),
        @Index(name = "idx_auth_activity_email", columnList = "administrator_email"),
        @Index(name = "idx_auth_activity_session", columnList = "session_id"),
        @Index(name = "idx_auth_activity_time", columnList = "occurred_at"),
        @Index(name = "idx_auth_activity_outcome", columnList = "outcome"),
        @Index(name = "idx_auth_activity_severity", columnList = "severity")
    }
)
public class AuthenticationActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "administrator_type", nullable = false, length = 30)
    private String administratorType;

    @Column(name = "administrator_id")
    private Long administratorId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "administrator_email", nullable = false, length = 255)
    private String administratorEmail;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity = "INFO";

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "event_description", length = 1000)
    private String eventDescription;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    public AuthenticationActivity() {
    }

    public Long getId() {
        return id;
    }

    public String getAdministratorType() {
        return administratorType;
    }

    public void setAdministratorType(String administratorType) {
        this.administratorType = administratorType;
    }

    public Long getAdministratorId() {
        return administratorId;
    }

    public void setAdministratorId(Long administratorId) {
        this.administratorId = administratorId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getAdministratorEmail() {
        return administratorEmail;
    }

    public void setAdministratorEmail(String administratorEmail) {
        this.administratorEmail = administratorEmail;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
