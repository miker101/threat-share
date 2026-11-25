
package com.example.demo.dto;

import java.time.LocalDateTime;

public record OrganizationProfileResponse(
    Long id,
    String name,
    String email,
    String status,
    LocalDateTime createdAt
) {}
