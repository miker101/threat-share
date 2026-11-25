
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.OrganizationCryptographicKey;

@Repository
public interface OrganizationCryptographicKeyRepository
        extends JpaRepository<OrganizationCryptographicKey, Long> {

    List<OrganizationCryptographicKey>
    findAllByOrganization_IdOrderByCreatedAtDesc(Long organizationId);

    Optional<OrganizationCryptographicKey>
    findByIdAndOrganization_Id(Long keyId, Long organizationId);

    Optional<OrganizationCryptographicKey>
    findFirstByOrganization_IdAndKeyPurposeAndKeyStatusOrderByKeyVersionDesc(
            Long organizationId,
            String keyPurpose,
            String keyStatus
    );
}
