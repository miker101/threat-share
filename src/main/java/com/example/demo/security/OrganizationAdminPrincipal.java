package com.example.demo.security;

public record OrganizationAdminPrincipal(
        Long userId,
        String email,
        Long organizationId
) {}