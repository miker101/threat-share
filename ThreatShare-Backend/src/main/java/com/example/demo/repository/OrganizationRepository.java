
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Organization;

public interface OrganizationRepository
        extends JpaRepository<Organization, Long> {

    Optional<Organization> findByEmail(String email);

    boolean existsByName(String name);

    // Retrieve organizations with a particular status,
    // ordered from oldest registration to newest.
    List<Organization> findByStatusOrderByCreatedAtAsc(String status);
    long countByStatus(String status);
}
