
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ServiceMonitoringResponse;
import com.example.demo.service.SystemServiceMonitoringService;

@RestController
@RequestMapping("/api/admin/system-health/services")
public class SystemServiceMonitoringController {

    private final SystemServiceMonitoringService monitoringService;

    public SystemServiceMonitoringController(
            SystemServiceMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping
    public ServiceMonitoringResponse getServiceMonitoring() {
        return monitoringService.monitorServices();
    }
}
