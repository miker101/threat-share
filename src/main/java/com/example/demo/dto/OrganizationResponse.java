package com.example.demo.dto;

import java.time.LocalDateTime;

public class OrganizationResponse {

    private Long id;
    private String name;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    public OrganizationResponse(
            Long id,
            String name,
            String email,
            String status,
            LocalDateTime createdAt) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}