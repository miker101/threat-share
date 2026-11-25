
package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.PlatformNotification;

public interface PlatformNotificationRepository
        extends JpaRepository<PlatformNotification, Long> {

    // Existing: retrieve all notifications for an admin.
    List<PlatformNotification> findByAdmin_IdOrderByCreatedAtDesc(
            Long adminId);

    // Existing: retrieve unread notifications.
    List<PlatformNotification> findByAdmin_IdAndReadAtIsNullOrderByCreatedAtDesc(
            Long adminId);

    // Existing: count unread notifications.
    long countByAdmin_IdAndReadAtIsNull(Long adminId);

    // Existing: retrieve one notification belonging to an admin.
    Optional<PlatformNotification> findByIdAndAdmin_Id(
            Long notificationId, Long adminId);

    // New: retrieve the latest notifications using pagination.
    Page<PlatformNotification> findByAdmin_IdOrderByCreatedAtDesc(
            Long adminId, Pageable pageable);

    // New: retrieve notifications received within a selected date.
    Page<PlatformNotification>
            findByAdmin_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                    Long adminId,
                    LocalDateTime startDate,
                    LocalDateTime endDate,
                    Pageable pageable);
}
