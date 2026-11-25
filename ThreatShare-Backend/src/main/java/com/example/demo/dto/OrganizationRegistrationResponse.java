
package com.example.demo.dto;

public record OrganizationRegistrationResponse(
        String message,
        Long organizationId,
        String organizationStatus,
        String administratorStatus
) {}
