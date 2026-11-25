
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.NetworkMonitoringService;
import com.example.demo.service.NetworkMonitoringService.NetworkMonitoringResponse;

@RestController
@RequestMapping("/api/admin/system-health/network")
public class NetworkMonitoringController {

    private final NetworkMonitoringService networkMonitoringService;

    public NetworkMonitoringController(
            NetworkMonitoringService networkMonitoringService) {
        this.networkMonitoringService = networkMonitoringService;
    }

    @GetMapping
    public NetworkMonitoringResponse getNetworkStats() {
        return networkMonitoringService.getNetworkStats();
    }
}
