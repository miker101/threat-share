
package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ThreatUpdateRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        String description,

        @NotBlank
        @Size(max = 60)
        String category,

        @NotBlank
        @Size(max = 30)
        String severity,

        @Size(max = 255)
        String source,

        LocalDateTime firstObserved,

        LocalDateTime lastObserved,

        @NotNull
        List<@Valid ThreatIndicatorRequest> indicators

) {}
