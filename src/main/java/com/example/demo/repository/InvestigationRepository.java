
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Investigation;

@Repository
public interface InvestigationRepository
        extends JpaRepository<Investigation, Long> {

    // Retrieve investigations belonging to one organization.
    List<Investigation> findAllByOrganization_IdOrderByCreatedAtDesc(
            Long organizationId);

    // Retrieve one investigation while enforcing organization ownership.
    Optional<Investigation> findByIdAndOrganization_Id(
            Long investigationId, Long organizationId);

    // Statistics for the current organization.
    long countByOrganization_Id(Long organizationId);

    long countByOrganization_IdAndStatus(
            Long organizationId, String status);

}
