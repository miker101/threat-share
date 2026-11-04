
package com.example.demo.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.OrganizationRepository;

@RestController
@RequestMapping("/api/admin/organizations")
public class OrganizationSummaryController {

    private final OrganizationRepository organizationRepository;

    public OrganizationSummaryController(
            OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

   
    @GetMapping("/count")
    public long getOrganizationCount() {
        return organizationRepository.count();
    }

    // New endpoint: counts grouped by organization status
    @GetMapping("/counts")
    public Map<String, Long> getOrganizationCounts() {
        return Map.of(
            "total", organizationRepository.count(),
            "pending", organizationRepository.countByStatus("PENDING"),
            "active", organizationRepository.countByStatus("ACTIVE"),
            "rejected", organizationRepository.countByStatus("REJECTED")
        );
    }
}
