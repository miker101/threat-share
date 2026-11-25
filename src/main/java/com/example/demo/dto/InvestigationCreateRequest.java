
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InvestigationCreateRequest(

        @NotBlank(message = "Investigation title is required.")
        @Size(max = 200, message = "Title cannot exceed 200 characters.")
        String title,

        String description,

        @NotBlank(message = "Investigation type is required.")
        @Size(max = 60)
        String investigationType,

        String priority,

        @Size(max = 150)
        String leadInvestigator

) {
}
