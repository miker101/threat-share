
package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.entity.PlatformNotification;

public class PlatformNotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String category;
    private String eventType;
    private String severity;
    private String referenceType;
    private Long referenceId;
    private String actionUrl;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public PlatformNotificationResponse(PlatformNotification notification) {

        this.id = notification.getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.category = notification.getCategory();
        this.eventType = notification.getEventType();
        this.severity = notification.getSeverity();
        this.referenceType = notification.getReferenceType();
        this.referenceId = notification.getReferenceId();
        this.actionUrl = notification.getActionUrl();
        this.read = notification.getReadAt() != null;
        this.createdAt = notification.getCreatedAt();
        this.readAt = notification.getReadAt();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getCategory() { return category; }
    public String getEventType() { return eventType; }
    public String getSeverity() { return severity; }
    public String getReferenceType() { return referenceType; }
    public Long getReferenceId() { return referenceId; }
    public String getActionUrl() { return actionUrl; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
}
