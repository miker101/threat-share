
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Organization;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

public interface OrganizationRepository
        extends JpaRepository<Organization, Long> {

    Optional<Organization> findByEmail(String email);

    boolean existsByName(String name);

    // Retrieve organizations with a particular status,
    // ordered from oldest registration to newest.
    List<Organization> findByStatusOrderByCreatedAtAsc(String status);
    @Query("SELECT COUNT(o) FROM Organization o WHERE o.status = :status")
	long countByStatus(String status);
    
    List<Organization> findTop5ByOrderByCreatedAtDesc();
    List<Organization> findAllByOrderByCreatedAtDesc();
    
 // Locks an organization row while its approval status is being changed.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Organization o WHERE o.id = :id")
    Optional<Organization> findByIdForUpdate(@Param("id") Long id);
}
