package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminDashboardResponse {

    private long totalOrganizations;
    private long activeOrganizations;
    private long pendingOrganizations;
    private long totalAdministrators;
    private long totalThreats;

    private Map<String, Long> threatSeverity;

    private List<ActivityItem> recentActivity;
    private List<OrganizationItem> recentOrganizations;

    private SystemHealth systemHealth;

    public long getTotalOrganizations() {
        return totalOrganizations;
    }

    public void setTotalOrganizations(long totalOrganizations) {
        this.totalOrganizations = totalOrganizations;
    }

    public long getActiveOrganizations() {
        return activeOrganizations;
    }

    public void setActiveOrganizations(long activeOrganizations) {
        this.activeOrganizations = activeOrganizations;
    }

    public long getPendingOrganizations() {
        return pendingOrganizations;
    }

    public void setPendingOrganizations(long pendingOrganizations) {
        this.pendingOrganizations = pendingOrganizations;
    }

    public long getTotalAdministrators() {
        return totalAdministrators;
    }

    public void setTotalAdministrators(long totalAdministrators) {
        this.totalAdministrators = totalAdministrators;
    }

    public long getTotalThreats() {
        return totalThreats;
    }

    public void setTotalThreats(long totalThreats) {
        this.totalThreats = totalThreats;
    }

    public Map<String, Long> getThreatSeverity() {
        return threatSeverity;
    }

    public void setThreatSeverity(Map<String, Long> threatSeverity) {
        this.threatSeverity = threatSeverity;
    }

    public List<ActivityItem> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ActivityItem> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public List<OrganizationItem> getRecentOrganizations() {
        return recentOrganizations;
    }

    public void setRecentOrganizations(List<OrganizationItem> recentOrganizations) {
        this.recentOrganizations = recentOrganizations;
    }

    public SystemHealth getSystemHealth() {
        return systemHealth;
    }

    public void setSystemHealth(SystemHealth systemHealth) {
        this.systemHealth = systemHealth;
    }

    public static class ActivityItem {

        private Long id;
        private String actionType;
        private String description;
        private String actor;
        private String category;
        private String status;
        private LocalDateTime occurredAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getActor() {
            return actor;
        }

        public void setActor(String actor) {
            this.actor = actor;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getOccurredAt() {
            return occurredAt;
        }

        public void setOccurredAt(LocalDateTime occurredAt) {
            this.occurredAt = occurredAt;
        }
    }

    public static class OrganizationItem {

        private Long id;
        private String name;
        private String status;
        private LocalDateTime registeredAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getRegisteredAt() {
            return registeredAt;
        }

        public void setRegisteredAt(LocalDateTime registeredAt) {
            this.registeredAt = registeredAt;
        }
    }

    public static class SystemHealth {

        private String backend;
        private String database;
        private String security;

        public String getBackend() {
            return backend;
        }

        public void setBackend(String backend) {
            this.backend = backend;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getSecurity() {
            return security;
        }

        public void setSecurity(String security) {
            this.security = security;
        }
    }
}