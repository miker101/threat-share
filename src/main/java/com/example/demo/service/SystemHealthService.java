package com.example.demo.service;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;

import org.springframework.stereotype.Service;

import com.example.demo.dto.SystemHealthResponse;
@Service
public class SystemHealthService {

    private final HealthEndpoint healthEndpoint;

    public SystemHealthService(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    public SystemHealthResponse getSystemHealth() {

    	HealthDescriptor health = healthEndpoint.health();

    	Map<String, HealthDescriptor> components =
    	        health instanceof CompositeHealthDescriptor composite
    	                ? java.util.Optional.ofNullable(composite.getComponents())
    	                        .orElse(Map.of())
    	                : Map.of();

        String overallStatus =
                health.getStatus().getCode();

        String databaseStatus =
                getComponentStatus(components, "db");

        String diskStatus =
                getComponentStatus(components, "diskSpace");

        // Retrieve JVM runtime information.
        RuntimeMXBean runtime =
                ManagementFactory.getRuntimeMXBean();

        long uptime = runtime.getUptime();

        Runtime jvm = Runtime.getRuntime();

        long memoryUsed =
                jvm.totalMemory() - jvm.freeMemory();

        long memoryMax =
                jvm.maxMemory();

        long memoryCommitted =
                jvm.totalMemory();

        int processors =
                jvm.availableProcessors();

        // Retrieve actual operating system CPU metrics.
        double systemCpu = -1;
        double processCpu = -1;

        java.lang.management.OperatingSystemMXBean osBean =
                ManagementFactory.getOperatingSystemMXBean();

        if (osBean instanceof com.sun.management.OperatingSystemMXBean cpuBean) {

            double systemLoad = cpuBean.getCpuLoad();
            double processLoad = cpuBean.getProcessCpuLoad();

            if (systemLoad >= 0) {
                systemCpu = systemLoad * 100;
            }

            if (processLoad >= 0) {
                processCpu = processLoad * 100;
            }
        }

        return new SystemHealthResponse(
                "ThreatShare",
                overallStatus,
                databaseStatus,
                diskStatus,
                uptime,
                memoryUsed,
                memoryMax,
                memoryCommitted,
                processors,
                systemCpu,
                processCpu
        );
    }

    private String getComponentStatus(
            Map<String, HealthDescriptor> components,
            String componentName) {

        HealthDescriptor component = components.get(componentName);

        return component == null
                ? "UNKNOWN"
                : component.getStatus().getCode();
    }
}