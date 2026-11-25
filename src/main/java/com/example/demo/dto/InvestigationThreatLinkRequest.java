
package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InvestigationThreatLinkRequest(

        @NotNull
        Long threatId,

        @Size(max = 60)
        String relationshipType,

        String notes
) {}
