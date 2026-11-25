
package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.PlatformNotification;
import com.example.demo.entity.PlatformSetting;
import com.example.demo.entity.ThreatShare;
import com.example.demo.repository.PlatformNotificationRepository;
import com.example.demo.repository.PlatformSettingRepository;
import com.example.demo.repository.ThreatShareRepository;

@Service
public class PlatformNotificationService {

    private final PlatformNotificationRepository notificationRepository;
    private final PlatformSettingRepository settingRepository;
    private final ThreatShareRepository adminRepository;

    public PlatformNotificationService(
            PlatformNotificationRepository notificationRepository,
            PlatformSettingRepository settingRepository,
            ThreatShareRepository adminRepository) {

        this.notificationRepository = notificationRepository;
        this.settingRepository = settingRepository;
        this.adminRepository = adminRepository;
    }

    /**
     * Creates a notification for every active Platform Admin,
     * provided the relevant notification preference is enabled.
     */
    @Transactional
    public void createNotification(
            String title,
            String message,
            String category,
            String eventType,
            String severity,
            String referenceType,
            Long referenceId,
            String actionUrl) {

        if (!isCategoryEnabled(category)) {
            return;
        }

        List<ThreatShare> admins = adminRepository.findByActiveTrue();

        for (ThreatShare admin : admins) {

            PlatformNotification notification = new PlatformNotification();

            notification.setAdmin(admin);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setCategory(category);
            notification.setEventType(eventType);
            notification.setSeverity(
                    severity == null ? "INFO" : severity);
            notification.setReferenceType(referenceType);
            notification.setReferenceId(referenceId);
            notification.setActionUrl(actionUrl);
            notification.setCreatedAt(LocalDateTime.now());

            notificationRepository.save(notification);
        }
    }

    /**
     * Checks the four existing notification preference settings.
     */
    private boolean isCategoryEnabled(String category) {

        String settingKey;

        switch (category.toLowerCase()) {
            case "registration":
                settingKey = "registrationNotificationsEnabled";
                break;

            case "security":
                settingKey = "securityNotificationsEnabled";
                break;

            case "system":
                settingKey = "systemNotificationsEnabled";
                break;

            case "audit":
                settingKey = "auditNotificationsEnabled";
                break;

            default:
                return false;
        }

        return settingRepository.findBySettingKey(settingKey)
                .map(PlatformSetting::getSettingValue)
                .map(Boolean::parseBoolean)
                .orElse(getDefaultPreference(category));
    }

    private boolean getDefaultPreference(String category) {
        return !"audit".equalsIgnoreCase(category);
    }

    /**
     * Existing method: retrieve all notifications.
     */
    @Transactional(readOnly = true)
    public List<PlatformNotification> getNotifications(Long adminId) {

        return notificationRepository
                .findByAdmin_IdOrderByCreatedAtDesc(adminId);
    }

    /**
     * New: retrieve the latest notifications using pagination.
     */
    @Transactional(readOnly = true)
    public Page<PlatformNotification> getLatestNotifications(
            Long adminId, int page, int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 3);

        return notificationRepository
                .findByAdmin_IdOrderByCreatedAtDesc(
                        adminId,
                        PageRequest.of(safePage, safeSize));
    }

    /**
     * New: retrieve notifications received on a selected date.
     */
    @Transactional(readOnly = true)
    public Page<PlatformNotification> getNotificationsByDate(
            Long adminId, LocalDate date, int page, int size) {

        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.plusDays(1).atStartOfDay();

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 3);

        return notificationRepository
                .findByAdmin_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                        adminId,
                        startDate,
                        endDate,
                        PageRequest.of(safePage, safeSize));
    }

    /**
     * Existing: retrieve unread notifications.
     */
    @Transactional(readOnly = true)
    public List<PlatformNotification> getUnreadNotifications(Long adminId) {

        return notificationRepository
                .findByAdmin_IdAndReadAtIsNullOrderByCreatedAtDesc(adminId);
    }

    /**
     * Existing: count unread notifications.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long adminId) {

        return notificationRepository
                .countByAdmin_IdAndReadAtIsNull(adminId);
    }

    /**
     * Existing: mark one notification as read.
     */
    @Transactional
    public boolean markAsRead(Long notificationId, Long adminId) {

        return notificationRepository
                .findByIdAndAdmin_Id(notificationId, adminId)
                .map(notification -> {

                    if (notification.getReadAt() == null) {
                        notification.setReadAt(LocalDateTime.now());
                        notificationRepository.save(notification);
                    }

                    return true;
                })
                .orElse(false);
    }

    /**
     * Existing: mark all notifications as read.
     */
    @Transactional
    public int markAllAsRead(Long adminId) {

        List<PlatformNotification> unread =
                getUnreadNotifications(adminId);

        LocalDateTime now = LocalDateTime.now();

        unread.forEach(notification ->
                notification.setReadAt(now));

        notificationRepository.saveAll(unread);

        return unread.size();
    }
}
