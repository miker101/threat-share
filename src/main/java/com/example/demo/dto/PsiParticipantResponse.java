
package com.example.demo.dto;

import java.time.LocalDateTime;

public record PsiParticipantResponse(
        Long organizationId,
        String organizationName,
        String participantRole,
        String participationStatus,
        LocalDateTime joinedAt,
        LocalDateTime completedAt
) {
}
