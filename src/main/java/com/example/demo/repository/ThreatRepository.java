package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Threat;

import java.time.LocalDateTime;

@Repository
public interface ThreatRepository extends JpaRepository<Threat, Long> {

    // Retrieve only threats belonging to the authenticated organization.
    List<Threat> findAllByOrganization_IdOrderByCreatedAtDesc(
            Long organizationId
    );

    // Retrieve a specific threat only if it belongs to that organization.
    Optional<Threat> findByIdAndOrganization_Id(
            Long threatId,
            Long organizationId
    );
    long countBy();
 // Retrieve all threats across every organization for Platform Admin.
    List<Threat> findAllByOrderByCreatedAtDesc();

    @Query("""
        SELECT t.severity, COUNT(t)
        FROM Threat t
        GROUP BY t.severity
        """)
    List<Object[]> countThreatsBySeverity();
 // Retrieve threats recorded within a selected day, ordered chronologically.
    List<Threat> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            LocalDateTime startOfDay,
            LocalDateTime startOfNextDay
    );
}