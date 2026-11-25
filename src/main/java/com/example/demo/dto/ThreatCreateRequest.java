package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ThreatCreateRequest(

        @NotBlank(message = "Threat title is required.")
        @Size(max = 200, message = "Title cannot exceed 200 characters.")
        String title,

        @NotBlank(message = "Threat description is required.")
        String description,

        @NotBlank(message = "Threat category is required.")
        @Size(max = 60, message = "Category cannot exceed 60 characters.")
        String category,

        @NotBlank(message = "Threat severity is required.")
        @Size(max = 30, message = "Severity cannot exceed 30 characters.")
        String severity,

        @Size(max = 255, message = "Source cannot exceed 255 characters.")
        String source,

        LocalDateTime firstObserved,

        LocalDateTime lastObserved,

        List<@Valid ThreatIndicatorRequest> indicators

) {}