
package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationProfileRequest(

    @NotBlank(message = "Organization name is required.")
    @Size(max = 150, message = "Organization name cannot exceed 150 characters.")
    String name,

    @NotBlank(message = "Organization email is required.")
    @Email(message = "Please provide a valid email address.")
    @Size(max = 255, message = "Email cannot exceed 255 characters.")
    String email

) {}
