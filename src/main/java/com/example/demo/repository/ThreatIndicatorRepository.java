
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ThreatIndicator;

@Repository
public interface ThreatIndicatorRepository
        extends JpaRepository<ThreatIndicator, Long> {

    // Existing: Retrieve indicators belonging to a specific threat
    // owned by a specific organization.
    List<ThreatIndicator>
    findAllByThreat_IdAndThreat_Organization_IdOrderByCreatedAtDesc(
            Long threatId,
            Long organizationId
    );

    // Existing: Retrieve an indicator only if its parent threat
    // belongs to the specified organization.
    Optional<ThreatIndicator>
    findByIdAndThreat_Organization_Id(
            Long indicatorId,
            Long organizationId
    );

    // NEW: Retrieve all indicators belonging to one organization.
    List<ThreatIndicator>
    findAllByThreat_Organization_IdOrderByCreatedAtDesc(
            Long organizationId
    );

    // NEW: Retrieve indicators belonging to ACTIVE organizations.
    // The PSI/correlation service will use these records as inputs
    // for organization-specific analysis.
    List<ThreatIndicator>
    findAllByThreat_Organization_StatusOrderByCreatedAtDesc(
            String organizationStatus
    );
    
}
