
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.DigitalSignature;

@Repository
public interface DigitalSignatureRepository
        extends JpaRepository<DigitalSignature, Long> {

    List<DigitalSignature> findAllByThreat_IdOrderBySignedAtDesc(
            Long threatId);

    Optional<DigitalSignature> findByIdAndThreat_Id(
            Long signatureId, Long threatId);

    List<DigitalSignature> findAllBySigner_IdOrderBySignedAtDesc(
            Long signerId);
}
