
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.CryptographicOperation;

@Repository
public interface CryptographicOperationRepository
        extends JpaRepository<CryptographicOperation, Long> {

    List<CryptographicOperation>
    findAllByOrganization_IdOrderByCreatedAtDesc(Long organizationId);

    Optional<CryptographicOperation>
    findByOperationReference(String operationReference);

    boolean existsByOperationReference(String operationReference);

    List<CryptographicOperation>
    findAllByOperationTypeOrderByCreatedAtDesc(String operationType);

    List<CryptographicOperation>
    findAllByOperationStatusOrderByCreatedAtDesc(String operationStatus);
    
    Optional<CryptographicOperation> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );
}
