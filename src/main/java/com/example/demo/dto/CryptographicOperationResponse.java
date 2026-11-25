
package com.example.demo.dto;

import java.time.LocalDateTime;

public record CryptographicOperationResponse(
        Long id,
        String operationReference,
        String operationType,
        String algorithm,
        String operationStatus,
        String inputHash,
        String outputHash,
        String resultReference,
        String errorCode,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {}
