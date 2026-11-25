package com.example.demo.dto;

public record SystemHealthResponse(
        String applicationName,
        String overallStatus,
        String databaseStatus,
        String diskStatus,
        long uptimeSeconds,
        long memoryUsedBytes,
        long memoryMaxBytes,
        long memoryCommittedBytes,
        int availableProcessors,
        double systemCpuUsage,
        double processCpuUsage
) {}