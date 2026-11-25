package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.OrganizationResponse;
import com.example.demo.service.AdminOrganizationService;

import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/admin/organizations")
public class AdminOrganizationController {

    private final AdminOrganizationService organizationService;

    public AdminOrganizationController(
            AdminOrganizationService organizationService) {

        this.organizationService = organizationService;
    }

    // Retrieve all organizations or filter by status.
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getOrganizations(
            @RequestParam(required = false) String status) {

        if (status != null && !status.isBlank()) {

            return ResponseEntity.ok(
                    organizationService.getOrganizationsByStatus(status)
            );
        }

        return ResponseEntity.ok(
                organizationService.getAllOrganizations()
        );
    }
 // Retrieve a single organization by ID.
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                organizationService.getOrganizationById(id)
        );
    }
}