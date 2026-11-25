
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.PsiOperation;

@Repository
public interface PsiOperationRepository
        extends JpaRepository<PsiOperation, Long> {

    List<PsiOperation>
    findAllByRequestingOrganization_IdOrderByCreatedAtDesc(
            Long organizationId
    );

    List<PsiOperation>
    findAllByPsiStatusOrderByCreatedAtDesc(
            String psiStatus
    );
}
