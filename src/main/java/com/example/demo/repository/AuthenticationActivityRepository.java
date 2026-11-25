
package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AuthenticationActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository
public interface AuthenticationActivityRepository
        extends JpaRepository<AuthenticationActivity, Long> {

    // Latest authentication and security events
    Page<AuthenticationActivity> findAllByOrderByOccurredAtDesc(
            Pageable pageable
    );

    // Filter events by type
    Page<AuthenticationActivity> findByEventTypeOrderByOccurredAtDesc(
            String eventType,
            Pageable pageable
    );

    // Filter events by severity
    Page<AuthenticationActivity> findBySeverityOrderByOccurredAtDesc(
            String severity,
            Pageable pageable
    );

    // Filter events by type and severity
    Page<AuthenticationActivity> findByEventTypeAndSeverityOrderByOccurredAtDesc(
            String eventType,
            String severity,
            Pageable pageable
    );

    // Count authentication outcomes
    long countByEventTypeAndOutcome(String eventType, String outcome);

    // Count events within a time range
    long countByOccurredAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // Count failed login attempts
    long countByEventTypeAndOutcomeAndOccurredAtBetween(
            String eventType,
            String outcome,
            LocalDateTime start,
            LocalDateTime end
    );

    // Retrieve events for an administrator
    List<AuthenticationActivity>
    findByAdministratorTypeAndAdministratorIdOrderByOccurredAtDesc(
            String administratorType,
            Long administratorId
    );

    // Retrieve events for a particular session
    List<AuthenticationActivity>
    findBySessionIdOrderByOccurredAtDesc(String sessionId);
    
 // Count failed login attempts for an email within a time window
    long countByAdministratorEmailAndEventTypeAndOutcomeAndOccurredAtAfter(
            String email,
            String eventType,
            String outcome,
            LocalDateTime after
    );

    // Check whether a suspicious security event was already recorded
    long countByAdministratorEmailAndEventTypeAndOccurredAtAfter(
            String email,
            String eventType,
            LocalDateTime after
    );

    // Count failed login attempts originating from an IP address
    long countByIpAddressAndEventTypeAndOutcomeAndOccurredAtAfter(
            String ipAddress,
            String eventType,
            String outcome,
            LocalDateTime after
    );
    @Query("""
    	    SELECT a
    	    FROM AuthenticationActivity a
    	    WHERE
    	        (:eventType IS NULL OR a.eventType = :eventType)
    	        AND (:severity IS NULL OR a.severity = :severity)
    	        AND (
    	            :search IS NULL
    	            OR LOWER(a.administratorEmail) LIKE LOWER(CONCAT('%', :search, '%'))
    	            OR LOWER(a.ipAddress) LIKE LOWER(CONCAT('%', :search, '%'))
    	            OR LOWER(a.eventDescription) LIKE LOWER(CONCAT('%', :search, '%'))
    	        )
    	    ORDER BY a.occurredAt DESC
    	    """)
    	Page<AuthenticationActivity> searchSecurityEvents(
    	        @Param("eventType") String eventType,
    	        @Param("severity") String severity,
    	        @Param("search") String search,
    	        Pageable pageable
    	);
}
