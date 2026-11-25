
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SystemDiagnosticsResponse;
import com.example.demo.service.SystemDiagnosticsService;

@RestController
@RequestMapping("/api/admin/system-diagnostics")
public class SystemDiagnosticsController {

    private final SystemDiagnosticsService diagnosticsService;

    public SystemDiagnosticsController(
            SystemDiagnosticsService diagnosticsService) {
        this.diagnosticsService = diagnosticsService;
    }

    @PostMapping("/run")
    public SystemDiagnosticsResponse runDiagnostics() {
        return diagnosticsService.runDiagnostics();
    }
}
