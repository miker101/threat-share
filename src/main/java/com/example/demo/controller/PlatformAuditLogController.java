
package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PlatformAuditLogResponse;
import com.example.demo.service.PlatformAuditLogService;

import com.example.demo.dto.AuditIntegrityResponse;
import com.example.demo.service.AuditLogService;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class PlatformAuditLogController {

    private final PlatformAuditLogService platformAuditLogService;
    private final AuditLogService auditLogService;

    public PlatformAuditLogController(
            PlatformAuditLogService platformAuditLogService,
            AuditLogService auditLogService) {

        this.platformAuditLogService = platformAuditLogService;
        this.auditLogService = auditLogService;
    }

    // Retrieve all platform-wide audit events.
    @GetMapping
    public List<PlatformAuditLogResponse> getAllAuditLogs() {

        return platformAuditLogService.getAllAuditLogs();
    }

    // Retrieve one audit event by ID.
    @GetMapping("/{id}")
    public PlatformAuditLogResponse getAuditLogById(
            @PathVariable Long id) {

        return platformAuditLogService.getAuditLogById(id);
    }
 // Verify the integrity of the complete audit chain.
    @GetMapping("/verify")
    public AuditIntegrityResponse verifyAuditIntegrity() {

        return auditLogService.verifyIntegrity();
    }
}
