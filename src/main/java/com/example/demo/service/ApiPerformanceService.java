package com.example.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import com.example.demo.dto.ApiPerformanceResponse;

@Service
public class ApiPerformanceService {

    private final MeterRegistry meterRegistry;

    public ApiPerformanceService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public ApiPerformanceResponse getApiPerformance() {

        long totalRequests = 0;
        long successfulRequests = 0;
        long failedRequests = 0;

        double totalResponseTimeSeconds = 0;
        double maximumResponseTimeSeconds = 0;
        
        System.out.println("========== API METRICS DEBUG ==========");

        System.out.println("Total registered meters: "
                + meterRegistry.getMeters().size());

        meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName()
                        .equals("http.server.requests"))
                .forEach(meter -> System.out.println(
                        "HTTP METER: " + meter.getId()
                ));

        System.out.println("=======================================");

        var requestTimers = meterRegistry.find("http.server.requests")
                .timers();
        
        

        for (Timer timer : requestTimers) {

            long count = timer.count();

            if (count <= 0) {
                continue;
            }

            totalRequests += count;

            String status = timer.getId()
                    .getTag("status");

            if (status != null) {

                if (status.startsWith("2") || status.startsWith("3")) {
                    successfulRequests += count;

                } else if (status.startsWith("4") || status.startsWith("5")) {
                    failedRequests += count;
                }
            }

            totalResponseTimeSeconds += timer.totalTime(
                    java.util.concurrent.TimeUnit.SECONDS
            );

            maximumResponseTimeSeconds = Math.max(
                    maximumResponseTimeSeconds,
                    timer.max(java.util.concurrent.TimeUnit.SECONDS)
            );
        }

        double averageResponseTimeMs = totalRequests > 0
                ? (totalResponseTimeSeconds / totalRequests) * 1000
                : 0;

        double maximumResponseTimeMs =
                maximumResponseTimeSeconds * 1000;

        return new ApiPerformanceResponse(
                totalRequests,
                successfulRequests,
                failedRequests,
                averageResponseTimeMs,
                maximumResponseTimeMs,
                LocalDateTime.now()
        );
    }
}