
package com.example.demo.crypto.he;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record HeResult(

        String operationReference,

        String algorithm,

        String status,

        int participantCount,

        String keyId,

        BigInteger encryptedAggregate,

        LocalDateTime completedAt

) {

    public HeResult {

        if (operationReference == null || operationReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Operation reference is required."
            );
        }

        if (!"PAILLIER".equalsIgnoreCase(algorithm)) {
            throw new IllegalArgumentException(
                    "Unsupported homomorphic encryption algorithm."
            );
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Operation status is required."
            );
        }

        if (participantCount < 2) {
            throw new IllegalArgumentException(
                    "At least two participants are required."
            );
        }

        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException(
                    "Encryption key identifier is required."
            );
        }

        if (encryptedAggregate == null ||
                encryptedAggregate.signum() <= 0) {
            throw new IllegalArgumentException(
                    "A valid encrypted aggregate is required."
            );
        }

        if (completedAt == null) {
            throw new IllegalArgumentException(
                    "Completion timestamp is required."
            );
        }
    }
}
