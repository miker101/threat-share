
package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.OrganizationApprovalService;

@RestController
@RequestMapping("/api/admin/organizations")
public class OrganizationApprovalController {

    private final OrganizationApprovalService approvalService;

    public OrganizationApprovalController(
            OrganizationApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PatchMapping("/{organizationId}/approve")
    public ResponseEntity<Map<String, Object>> approveOrganization(
            @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                approvalService.approveOrganization(organizationId));
    }

    @PatchMapping("/{organizationId}/reject")
    public ResponseEntity<Map<String, Object>> rejectOrganization(
            @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                approvalService.rejectOrganization(organizationId));
    }
}
