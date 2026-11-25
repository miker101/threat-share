package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ThreatIndicatorRequest(

        @NotBlank(message = "Indicator type is required.")
        @Size(max = 40, message = "Indicator type cannot exceed 40 characters.")
        String indicatorType,

        @NotBlank(message = "Indicator value is required.")
        @Size(max = 512, message = "Indicator value cannot exceed 512 characters.")
        String indicatorValue,

        String description,

        @DecimalMin(value = "0.00", message = "Confidence cannot be below 0.")
        @DecimalMax(value = "100.00", message = "Confidence cannot exceed 100.")
        BigDecimal confidenceLevel,

        LocalDateTime firstObserved,

        LocalDateTime lastObserved

) {}