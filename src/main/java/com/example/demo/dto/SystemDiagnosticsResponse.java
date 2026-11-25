
package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SystemDiagnosticsResponse(
        String overallStatus,
        String summary,
        LocalDateTime executedAt,
        List<SystemDiagnosticCheck> checks
) {}
