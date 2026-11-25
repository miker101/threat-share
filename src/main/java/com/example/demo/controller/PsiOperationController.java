
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.PsiOperationResponse;
import com.example.demo.entity.PsiOperation;
import com.example.demo.service.PsiOperationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.example.demo.dto.PsiParticipantResponse;
import java.util.List;
@RestController
@RequestMapping("/api/organization/psi/operations")
public class PsiOperationController {

    private final PsiOperationService psiOperationService;

    public PsiOperationController(PsiOperationService psiOperationService) {
        this.psiOperationService = psiOperationService;
    }

    @PostMapping
    public ResponseEntity<PsiOperationResponse> createOperation(
            @Valid @RequestBody CreatePsiOperationRequest request) { {

        PsiOperation operation =
                psiOperationService.createOperation(request.purpose());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(operation));
   
            }
    }

    @GetMapping
    public ResponseEntity<List<PsiOperationResponse>> getMyOperations() {

        List<PsiOperationResponse> operations =
                psiOperationService.getMyOperations()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(operations);
    }

    private PsiOperationResponse toResponse(PsiOperation operation) {

        return new PsiOperationResponse(
                operation.getId(),
                operation.getCryptographicOperation().getOperationReference(),
                operation.getProtocolName(),
                operation.getProtocolVersion(),
                operation.getPurpose(),
                operation.getParticipantCount(),
                operation.getPsiStatus(),
                operation.getCreatedAt()
        );
    }

    
    public record CreatePsiOperationRequest(
            @NotBlank(message = "Purpose is required")
            @Size(max = 2000, message = "Purpose cannot exceed 2000 characters")
            String purpose
    ) {
    }
    

@GetMapping("/{operationId}/participants")
public ResponseEntity<List<PsiParticipantResponse>> getParticipants(
        @PathVariable Long operationId) {

    List<PsiParticipantResponse> participants =
            psiOperationService.getParticipants(operationId)
                    .stream()
                    .map(p -> new PsiParticipantResponse(
                            p.getOrganization().getId(),
                            p.getOrganization().getName(),
                            p.getParticipantRole(),
                            p.getParticipationStatus(),
                            p.getJoinedAt(),
                            p.getCompletedAt()
                    ))
                    .toList();

    return ResponseEntity.ok(participants);
}

@PostMapping("/{operationId}/join")
public ResponseEntity<PsiParticipantResponse> joinOperation(
        @PathVariable Long operationId) {

    var participant =
            psiOperationService.joinOperation(operationId);

    return ResponseEntity.ok(
            new PsiParticipantResponse(
                    participant.getOrganization().getId(),
                    participant.getOrganization().getName(),
                    participant.getParticipantRole(),
                    participant.getParticipationStatus(),
                    participant.getJoinedAt(),
                    participant.getCompletedAt()
            )
    );
}

}
