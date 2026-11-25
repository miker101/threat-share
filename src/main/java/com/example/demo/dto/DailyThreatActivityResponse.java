package com.example.demo.dto;

import java.time.LocalDateTime;

public class DailyThreatActivityResponse {

    private Long id;
    private String title;
    private String severity;
    private LocalDateTime createdAt;

    public DailyThreatActivityResponse(
            Long id,
            String title,
            String severity,
            LocalDateTime createdAt) {

        this.id = id;
        this.title = title;
        this.severity = severity;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSeverity() {
        return severity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}