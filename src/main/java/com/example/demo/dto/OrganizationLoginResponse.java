package com.example.demo.dto;

public record OrganizationLoginResponse(

        String message,

        String token,

        String tokenType,

        Long userId,

        String fullName,

        String email,

        Long organizationId,

        String organizationName,

        String role

) {}