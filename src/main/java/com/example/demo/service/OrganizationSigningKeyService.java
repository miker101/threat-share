package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CryptoKeyRegistrationResponse;
import com.example.demo.entity.OrganizationCryptographicKey;
import com.example.demo.entity.User;
import com.example.demo.repository.OrganizationCryptographicKeyRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.OrganizationAdminPrincipal;

@Service
public class OrganizationSigningKeyService {

	private static final String KEY_PURPOSE = "DIGITAL_SIGNATURE";
    private static final String ALGORITHM = "RSASSA-PSS";

    private final UserRepository userRepository;
    private final OrganizationCryptographicKeyRepository keyRepository;
    private final RsaKeyGenerationService keyGenerationService;
    private final CryptoKeyStoreService keyStoreService;

    public OrganizationSigningKeyService(
            UserRepository userRepository,
            OrganizationCryptographicKeyRepository keyRepository,
            RsaKeyGenerationService keyGenerationService,
            CryptoKeyStoreService keyStoreService) {

        this.userRepository = userRepository;
        this.keyRepository = keyRepository;
        this.keyGenerationService = keyGenerationService;
        this.keyStoreService = keyStoreService;
    }

    @Transactional
    public CryptoKeyRegistrationResponse registerSigningKey()
            throws Exception {

        // 1. Obtain the authenticated organization administrator.
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof OrganizationAdminPrincipal principal)) {

            throw new SecurityException(
                    "Authenticated organization administrator required.");
        }

        // 2. Load the actual user and organization from the database.
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user was not found."));

        if (!user.getStatus().equals("ACTIVE")) {
            throw new SecurityException("User account is not active.");
        }

        if (user.getOrganization() == null
                || !user.getOrganization().getId()
                        .equals(principal.organizationId())) {

            throw new SecurityException(
                    "Organization identity verification failed.");
        }

        Long organizationId = principal.organizationId();

        // 3. Determine the next version, including previously revoked keys.
        int nextVersion = keyRepository
                .findAllByOrganization_IdOrderByCreatedAtDesc(
                        organizationId)
                .stream()
                .filter(key -> KEY_PURPOSE.equals(key.getKeyPurpose()))
                .mapToInt(OrganizationCryptographicKey::getKeyVersion)
                .max()
                .orElse(0) + 1;

        // 4. Generate the RSA key pair and certificate.
        var generated = keyGenerationService.generateForOrganization(
                organizationId,
                nextVersion
        );

        String alias = "org-" + organizationId
                + "-threat-signing-v" + nextVersion;

        // 5. Prepare the public key and fingerprint.
        String publicKeyPem = toPem(
                generated.keyPair().getPublic().getEncoded()
        );

        String fingerprint = generateFingerprint(
                generated.keyPair().getPublic().getEncoded()
        );

        // 6. Store the private key in the PKCS12 keystore.
        keyStoreService.storePrivateKey(
                alias,
                generated.keyPair().getPrivate(),
                generated.certificate()
        );

        try {

            // 7. Register the public key in the database.
            OrganizationCryptographicKey key =
                    new OrganizationCryptographicKey();

            key.setOrganization(user.getOrganization());
            key.setCreatedBy(user);

            key.setKeyPurpose(KEY_PURPOSE);
            key.setKeyType("SIGNING");
            key.setAlgorithm(ALGORITHM);
            key.setKeyVersion(nextVersion);

            key.setPublicKeyMaterial(publicKeyPem);
            key.setKeyFormat("X.509");
            key.setKeyFingerprint(fingerprint);

            key.setProtectedKeyReference(alias);
            key.setKeyStatus("ACTIVE");

            key.setActivatedAt(java.time.LocalDateTime.now());

            key.setExpiresAt(
                    generated.certificate()
                            .getNotAfter()
                            .toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
            );

            OrganizationCryptographicKey saved =
                    keyRepository.saveAndFlush(key);

            // 8. Return public registration details only.
            return new CryptoKeyRegistrationResponse(
                    saved.getId(),
                    organizationId,
                    saved.getKeyPurpose(),
                    saved.getAlgorithm(),
                    saved.getKeyVersion(),
                    saved.getPublicKeyMaterial(),
                    saved.getKeyFingerprint(),
                    saved.getKeyStatus(),
                    saved.getExpiresAt()
            );

        } catch (Exception exception) {

            // Compensate if database registration fails.
            try {
                keyStoreService.removePrivateKey(alias);
            } catch (Exception cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }

    private String toPem(byte[] encodedKey) {

        String base64 = Base64.getMimeEncoder(
                64,
                "\n".getBytes(StandardCharsets.UTF_8)
        ).encodeToString(encodedKey);

        return "-----BEGIN PUBLIC KEY-----\n"
                + base64
                + "\n-----END PUBLIC KEY-----";
    }

    private String generateFingerprint(byte[] encodedKey)
            throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        return HexFormat.of().formatHex(
                digest.digest(encodedKey)
        );
    }
}