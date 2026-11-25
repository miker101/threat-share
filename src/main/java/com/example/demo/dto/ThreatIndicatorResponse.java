package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ThreatIndicatorResponse(

        Long id,
        String indicatorType,
        String indicatorValue,
        String description,
        BigDecimal confidenceLevel,
        LocalDateTime firstObserved,
        LocalDateTime lastObserved,
        LocalDateTime createdAt

) {}