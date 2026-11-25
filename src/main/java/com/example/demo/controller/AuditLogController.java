
package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ActivityLogResponse;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.AuditLogQueryService;

@RestController
@RequestMapping("/api/organization/audit-logs")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;
    private final CurrentUserService currentUserService;

    public AuditLogController(
            AuditLogQueryService auditLogQueryService,
            CurrentUserService currentUserService) {

        this.auditLogQueryService = auditLogQueryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ActivityLogResponse> getOrganizationActivities() {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        return auditLogQueryService
                .getOrganizationActivities(organizationId);
    }
}
