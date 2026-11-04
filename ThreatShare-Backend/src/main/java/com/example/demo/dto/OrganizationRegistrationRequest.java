
package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrganizationRegistrationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 150, message = "Organization name must not exceed 150 characters")
    private String organizationName;

    @NotBlank(message = "Organization email is required")
    @Email(message = "Enter a valid organization email")
    @Size(max = 255)
    private String organizationEmail;

    @NotBlank(message = "Administrator name is required")
    @Size(max = 100)
    private String adminFullName;

    @NotBlank(message = "Administrator email is required")
    @Email(message = "Enter a valid administrator email")
    @Size(max = 255)
    private String adminEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 12, max = 72,
          message = "Password must be between 12 and 72 characters")
    private String password;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationEmail() {
        return organizationEmail;
    }

    public void setOrganizationEmail(String organizationEmail) {
        this.organizationEmail = organizationEmail;
    }

    public String getAdminFullName() {
        return adminFullName;
    }

    public void setAdminFullName(String adminFullName) {
        this.adminFullName = adminFullName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
