
package com.example.demo.dto;

import java.time.LocalDateTime;

public record AuditIntegrityResponse(

        boolean valid,

        String message,

        Long failedEventId,

        int totalEvents,

        LocalDateTime verifiedAt

) {}
