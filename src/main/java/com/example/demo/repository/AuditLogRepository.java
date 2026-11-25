
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Locks the first audit record until the current transaction commits.
    // This serializes audit-chain writes.
    @Query(value = "SELECT id FROM audit_logs WHERE id = 1 FOR UPDATE",
           nativeQuery = true)
    Optional<Long> lockAuditChain();

    // Retrieves all audit records in chronological insertion order.
    List<AuditLog> findAllByOrderByIdAsc();

    Optional<AuditLog> findTopByOrderByIdDesc();

    // Retrieves only the authenticated organization's audit records.
    List<AuditLog> findAllByOrganization_IdOrderByOccurredAtDesc(
            Long organizationId);

    // Retrieves one audit record belonging to a specific organization.
    Optional<AuditLog> findByIdAndOrganization_Id(
            Long id, Long organizationId);
    List<AuditLog> findTop5ByOrderByOccurredAtDesc();
}
