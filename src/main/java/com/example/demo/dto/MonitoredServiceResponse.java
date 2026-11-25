package com.example.demo.dto;

import java.time.LocalDateTime;

public record MonitoredServiceResponse(
        String serviceName,
        String status,
        String message,
        double responseTimeMs,
        LocalDateTime checkedAt
) {}