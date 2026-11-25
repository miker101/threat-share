package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.SystemIncident;

@Repository
public interface SystemIncidentRepository
        extends JpaRepository<SystemIncident, Long> {

    Optional<SystemIncident> findFirstByComponentAndStatusOrderByCreatedAtDesc(
            String component,
            String status
    );

    List<SystemIncident> findTop20ByOrderByCreatedAtDesc();

    List<SystemIncident> findTop20ByStatusOrderByCreatedAtDesc(
            String status
    );
}