
package com.example.demo.dto;

import java.time.LocalDateTime;

public record PsiOperationResponse(
        Long id,
        String operationReference,
        String protocolName,
        String protocolVersion,
        String purpose,
        Integer participantCount,
        String psiStatus,
        LocalDateTime createdAt
) {
}
