
package com.example.demo.dto;

import java.time.LocalDateTime;

public class PendingOrganizationResponse {

    private Long organizationId;
    private String organizationName;
    private String organizationEmail;
    private String organizationStatus;

    private String adminFullName;
    private String adminEmail;
    private String adminStatus;

    private LocalDateTime registeredAt;

    public PendingOrganizationResponse(
            Long organizationId,
            String organizationName,
            String organizationEmail,
            String organizationStatus,
            String adminFullName,
            String adminEmail,
            String adminStatus,
            LocalDateTime registeredAt) {

        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.organizationEmail = organizationEmail;
        this.organizationStatus = organizationStatus;
        this.adminFullName = adminFullName;
        this.adminEmail = adminEmail;
        this.adminStatus = adminStatus;
        this.registeredAt = registeredAt;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getOrganizationEmail() {
        return organizationEmail;
    }

    public String getOrganizationStatus() {
        return organizationStatus;
    }

    public String getAdminFullName() {
        return adminFullName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getAdminStatus() {
        return adminStatus;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
