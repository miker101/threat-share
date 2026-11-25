package com.example.demo.security;

public record PlatformAdminPrincipal(
        Long adminId,
        String email
) {}