
package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PendingOrganizationResponse;
import com.example.demo.service.PendingOrganizationService;

@RestController
@RequestMapping("/api/admin/organizations")
public class PendingOrganizationController {

    private final PendingOrganizationService pendingOrganizationService;

    public PendingOrganizationController(
            PendingOrganizationService pendingOrganizationService) {
        this.pendingOrganizationService = pendingOrganizationService;
    }

    @GetMapping("/pending")
    public List<PendingOrganizationResponse> getPendingOrganizations() {
        return pendingOrganizationService.getPendingOrganizations();
    }
}
