
package com.example.demo.dto;

import java.time.LocalDateTime;

public record SystemDiagnosticCheck(
        String component,
        String status,
        String message,
        LocalDateTime checkedAt
) {}
