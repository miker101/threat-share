package com.example.demo.dto;

import java.time.LocalDateTime;

public record CryptoKeyRegistrationResponse(
        Long keyId,
        Long organizationId,
        String keyPurpose,
        String algorithm,
        Integer keyVersion,
        String publicKeyMaterial,
        String keyFingerprint,
        String keyStatus,
        LocalDateTime expiresAt
) {}