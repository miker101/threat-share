package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ServiceMonitoringResponse(
        String overallStatus,
        int activeServices,
        int totalServices,
        double availabilityPercentage,
        double averageResponseTimeMs,
        LocalDateTime checkedAt,
        List<MonitoredServiceResponse> services
) {}