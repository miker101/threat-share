
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CryptographicOperationResponse;
import com.example.demo.entity.CryptographicOperation;
import com.example.demo.repository.CryptographicOperationRepository;
import com.example.demo.security.CurrentUserService;

@RestController
@RequestMapping("/api/organization/cryptographic-operations")
public class CryptographicOperationController {

    private final CryptographicOperationRepository operationRepository;
    private final CurrentUserService currentUserService;

    public CryptographicOperationController(
            CryptographicOperationRepository operationRepository,
            CurrentUserService currentUserService) {

        this.operationRepository = operationRepository;
        this.currentUserService = currentUserService;
    }

    // Get all cryptographic operations belonging to the logged-in organization
    @GetMapping
    public ResponseEntity<List<CryptographicOperationResponse>> getOperations() {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        List<CryptographicOperationResponse> operations =
                operationRepository
                        .findAllByOrganization_IdOrderByCreatedAtDesc(organizationId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(operations);
    }

    // Get one operation, but only if it belongs to the logged-in organization
    @GetMapping("/{id}")
    public ResponseEntity<CryptographicOperationResponse> getOperation(
            @PathVariable Long id) {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        return operationRepository
                .findByIdAndOrganization_Id(id, organizationId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private CryptographicOperationResponse toResponse(
            CryptographicOperation operation) {

        return new CryptographicOperationResponse(
                operation.getId(),
                operation.getOperationReference(),
                operation.getOperationType(),
                operation.getAlgorithm(),
                operation.getOperationStatus(),
                operation.getInputHash(),
                operation.getOutputHash(),
                operation.getResultReference(),
                operation.getErrorCode(),
                operation.getStartedAt(),
                operation.getCompletedAt(),
                operation.getCreatedAt()
        );
    }
}
