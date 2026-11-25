
package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.PlatformNotificationResponse;
import com.example.demo.entity.PlatformNotification;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.PlatformNotificationService;

@RestController
@RequestMapping("/api/admin/notifications")
public class PlatformNotificationController {

    private final PlatformNotificationService notificationService;
    private final CurrentUserService currentUserService;

    public PlatformNotificationController(
            PlatformNotificationService notificationService,
            CurrentUserService currentUserService) {

        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    // Get the latest notifications with pagination
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {

        Long adminId = currentUserService.getCurrentPlatformAdminId();

        Page<PlatformNotification> result =
                notificationService.getLatestNotifications(
                        adminId, page, size);

        return ResponseEntity.ok(buildPageResponse(result));
    }

    // Get notifications received on a selected date
    @GetMapping(params = "date")
    public ResponseEntity<Map<String, Object>> getNotificationsByDate(
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {

        Long adminId = currentUserService.getCurrentPlatformAdminId();

        Page<PlatformNotification> result =
                notificationService.getNotificationsByDate(
                        adminId, date, page, size);

        return ResponseEntity.ok(buildPageResponse(result));
    }

    // Build a consistent paginated response
    private Map<String, Object> buildPageResponse(
            Page<PlatformNotification> result) {

        List<PlatformNotificationResponse> notifications =
                result.getContent()
                        .stream()
                        .map(PlatformNotificationResponse::new)
                        .toList();

        return Map.of(
                "notifications", notifications,
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", result.getNumber(),
                "pageSize", result.getSize(),
                "hasNext", result.hasNext(),
                "hasPrevious", result.hasPrevious()
        );
    }

    // Get all notifications belonging to the authenticated admin
    @GetMapping
    public ResponseEntity<List<PlatformNotificationResponse>> getNotifications() {

        Long adminId = currentUserService.getCurrentPlatformAdminId();

        List<PlatformNotificationResponse> notifications =
                notificationService.getNotifications(adminId)
                        .stream()
                        .map(PlatformNotificationResponse::new)
                        .toList();

        return ResponseEntity.ok(notifications);
    }

    // Get unread notification count
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {

        Long adminId = currentUserService.getCurrentPlatformAdminId();

        long count = notificationService.getUnreadCount(adminId);

        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Get unread notifications
    @GetMapping("/unread")
    public ResponseEntity<List<PlatformNotificationResponse>> getUnreadNotifications() {

        Long adminId = currentUserService.getCurrentPlatformAdminId();

        List<PlatformNotificationResponse> notifications =
                notificationService.getUnreadNotifications(adminId)
                        .stream()
                        .map(PlatformNotificationResponse::new)
                        .toList();

        return ResponseEntity.ok(notifications);
    }

    // Mark one notification as read
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long notificationId) {

        Long adminId = currentUserService.getCurrentPlatformAdminId();

        boolean updated =
                notificationService.markAsRead(notificationId, adminId);

        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of("message", "Notification marked as read.")
        );
    }

    // Mark all notifications as read
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {

        Long adminId = currentUserService.getCurrentPlatformAdminId();

        int updated = notificationService.markAllAsRead(adminId);

        return ResponseEntity.ok(
                Map.of(
                        "message", "All notifications marked as read.",
                        "updatedCount", updated
                )
        );
    }
}
