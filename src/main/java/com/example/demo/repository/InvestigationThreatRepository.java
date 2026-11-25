
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.InvestigationThreat;

@Repository
public interface InvestigationThreatRepository extends JpaRepository<InvestigationThreat, Long> {

    List<InvestigationThreat> findAllByInvestigation_IdOrderByLinkedAtDesc(Long investigationId);

    Optional<InvestigationThreat> findByInvestigation_IdAndThreat_Id(
            Long investigationId,
            Long threatId
    );

    boolean existsByInvestigation_IdAndThreat_Id(
            Long investigationId,
            Long threatId
    );
}
