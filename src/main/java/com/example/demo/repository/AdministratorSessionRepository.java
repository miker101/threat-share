
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AdministratorSession;

import java.time.LocalDateTime;
@Repository
public interface AdministratorSessionRepository
        extends JpaRepository<AdministratorSession, Long> {

    Optional<AdministratorSession> findBySessionId(String sessionId);
    
    List<AdministratorSession> findByStatusAndExpiresAtLessThanEqual(
            String status,
            LocalDateTime currentTime
    );

    List<AdministratorSession> findByAdministratorTypeAndStatus(
            String administratorType,
            String status
    );

    List<AdministratorSession> findByAdministratorTypeAndAdministratorIdAndStatus(
            String administratorType,
            Long administratorId,
            String status
    );

    List<AdministratorSession> findByOrganizationIdAndStatus(
            Long organizationId,
            String status
    );
    
    long countByStatusAndExpiresAtAfter(
            String status,
            LocalDateTime currentTime
    );

    List<AdministratorSession> findAllByOrderByCreatedAtDesc();

    List<AdministratorSession> findByStatusOrderByCreatedAtDesc(
            String status
    );

}
