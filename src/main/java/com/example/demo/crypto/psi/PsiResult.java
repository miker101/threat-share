
package com.example.demo.crypto.psi;

import java.time.LocalDateTime;
import java.util.List;

public class PsiResult {

    private final String sessionId;
    private final String protocolName;
    private final String status;
    private final int participantCount;
    private final int intersectionSize;
    private final LocalDateTime completedAt;
    private final List<MatchReference> matches;

    public PsiResult(
            String sessionId,
            String protocolName,
            String status,
            int participantCount,
            int intersectionSize,
            LocalDateTime completedAt,
            List<MatchReference> matches) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID is required");
        }

        if (participantCount < 2) {
            throw new IllegalArgumentException(
                    "PSI requires at least two participants");
        }

        if (intersectionSize < 0) {
            throw new IllegalArgumentException(
                    "Intersection size cannot be negative");
        }

        this.sessionId = sessionId;
        this.protocolName = protocolName;
        this.status = status;
        this.participantCount = participantCount;
        this.intersectionSize = intersectionSize;
        this.completedAt = completedAt;
        this.matches = matches == null ? List.of() : List.copyOf(matches);
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public String getStatus() {
        return status;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public int getIntersectionSize() {
        return intersectionSize;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public List<MatchReference> getMatches() {
        return matches;
    }

    public record MatchReference(
            String indicatorType,
            Long localIndicatorId,
            Long matchedOrganizationId) {
    }
}
