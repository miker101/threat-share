
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.PsiOperationParticipant;

@Repository
public interface PsiOperationParticipantRepository
        extends JpaRepository<PsiOperationParticipant, Long> {

    List<PsiOperationParticipant>
    findAllByPsiOperation_IdOrderByCreatedAtAsc(
            Long psiOperationId
    );

    List<PsiOperationParticipant>
    findAllByOrganization_IdOrderByCreatedAtDesc(
            Long organizationId
    );

    Optional<PsiOperationParticipant>
    findByPsiOperation_IdAndOrganization_Id(
            Long psiOperationId,
            Long organizationId
    );

    long countByPsiOperation_IdAndParticipationStatus(
            Long psiOperationId,
            String participationStatus
    );
}
