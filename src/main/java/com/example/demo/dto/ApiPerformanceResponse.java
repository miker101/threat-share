package com.example.demo.dto;

import java.time.LocalDateTime;

public record ApiPerformanceResponse(
        long totalRequests,
        long successfulRequests,
        long failedRequests,
        double averageResponseTimeMs,
        double maximumResponseTimeMs,
        LocalDateTime checkedAt
) {}