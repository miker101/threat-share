
package com.example.demo.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.example.demo.crypto.he.HeEngine;
import com.example.demo.crypto.he.HeCryptoService.EncryptedValue;
import com.example.demo.crypto.he.HeCryptoService.PaillierKeyPair;
import com.example.demo.crypto.he.HeCryptoService.PaillierPublicKey;
import com.example.demo.crypto.he.HeCryptoService.PaillierPrivateKey;
import com.example.demo.crypto.zkp.ZkpEngine;
import com.example.demo.crypto.zkp.ZkpProofService.ZkpProof;
import com.example.demo.entity.CryptographicOperation;
import com.example.demo.entity.Organization;
import com.example.demo.entity.OrganizationCryptographicKey;
import com.example.demo.entity.User;
import com.example.demo.repository.CryptographicOperationRepository;
import com.example.demo.repository.OrganizationCryptographicKeyRepository;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CurrentUserService;

@Service
public class CryptographicOperationService {

    private static final String HE_ALGORITHM = "PAILLIER-2048";

    private static final String ZKP_ALGORITHM =
            "SCHNORR-P256-FIAT-SHAMIR-SHA256";

    private final HeEngine heEngine;
    private final ZkpEngine zkpEngine;

    private final CryptographicOperationRepository operationRepository;
    private final OrganizationCryptographicKeyRepository keyRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public CryptographicOperationService(
            HeEngine heEngine,
            ZkpEngine zkpEngine,
            CryptographicOperationRepository operationRepository,
            OrganizationCryptographicKeyRepository keyRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService) {

        this.heEngine = heEngine;
        this.zkpEngine = zkpEngine;
        this.operationRepository = operationRepository;
        this.keyRepository = keyRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    // --------------------------------------------------
    // HOMOMORPHIC ENCRYPTION
    // --------------------------------------------------

    public PaillierKeyPair generateHeKeys() {

        return executeRecorded(
                "HE_KEY_GENERATION",
                HE_ALGORITHM,
                null,
                null,
                heEngine::generateKeys,
                result -> result.getPublicKey()
                        .getModulus().toString(16),
                result -> "HE-KEY:" + result.getPublicKey().getKeyId()
        );
    }

    public EncryptedValue encryptHeValue(
            BigInteger plaintext,
            PaillierPublicKey publicKey) {

        return executeRecorded(
                "HE_ENCRYPTION",
                HE_ALGORITHM,
                null,
                null,
                () -> heEngine.encrypt(plaintext, publicKey),
                result -> result.getValue().toString(16),
                result -> "HE-CIPHERTEXT:" + result.getKeyId()
        );
    }

    public EncryptedValue aggregateHeValues(
            EncryptedValue first,
            EncryptedValue second,
            PaillierPublicKey publicKey) {

        String inputFingerprint = hash(
                first.getValue().toString(16)
                + second.getValue().toString(16)
        );

        return executeRecorded(
                "HE_AGGREGATION",
                HE_ALGORITHM,
                inputFingerprint,
                null,
                () -> heEngine.aggregate(first, second, publicKey),
                result -> result.getValue().toString(16),
                result -> "HE-AGGREGATE:" + result.getKeyId()
        );
    }

    /*
     * Decryption is a sensitive operation.
     * Do not expose this method through an unrestricted endpoint.
     */
    public BigInteger decryptHeValue(
            EncryptedValue encryptedValue,
            PaillierPrivateKey privateKey) {

        return executeRecorded(
                "HE_DECRYPTION",
                HE_ALGORITHM,
                null,
                null,
                () -> heEngine.decrypt(encryptedValue, privateKey),

                // Never store a hash of the plaintext.
                result -> null,

                result -> "HE-DECRYPTION-COMPLETED"
        );
    }

    // --------------------------------------------------
    // ZERO-KNOWLEDGE PROOFS
    // --------------------------------------------------

    /*
     * This method must only receive a private scalar from
     * trusted backend key-management code.
     *
     * Never accept the private scalar directly from a browser.
     */
    public ZkpProof generateZkpProof(
            BigInteger privateScalar,
            String context) {

        return executeRecorded(
                "ZKP_PROOF_GENERATION",
                ZKP_ALGORITHM,
                context == null ? null : hash(context),
                null,
                () -> zkpEngine.generateProof(privateScalar, context),

                result -> hash(
                        HexFormat.of().formatHex(result.publicKey())
                        + HexFormat.of().formatHex(result.commitment())
                        + result.response().toString(16)
                ),

                result -> "ZKP-PROOF-GENERATED"
        );
    }

    /*
     * Verification uses a public key registered to the
     * currently authenticated organization.
     */
    public boolean verifyZkpProof(
            ZkpProof proof,
            Long registeredKeyId) {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        OrganizationCryptographicKey registeredKey =
                keyRepository.findByIdAndOrganization_Id(
                        registeredKeyId,
                        organizationId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Registered key not found for your organization."
                        )
                );

        if (!"ACTIVE".equalsIgnoreCase(
                registeredKey.getKeyStatus())) {

            throw new IllegalArgumentException(
                    "The registered public key is not active."
            );
        }

        if (registeredKey.getAlgorithm() == null
                || !registeredKey.getAlgorithm()
                        .toUpperCase()
                        .contains("P-256")) {

            throw new IllegalArgumentException(
                    "The registered key must use P-256."
            );
        }

        String publicKeyMaterial =
                registeredKey.getPublicKeyMaterial();

        if (publicKeyMaterial == null
                || publicKeyMaterial.isBlank()) {

            throw new IllegalArgumentException(
                    "Registered public key material is missing."
            );
        }

        byte[] registeredPublicKey;

        try {
            registeredPublicKey =
                    Base64.getDecoder().decode(publicKeyMaterial);

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Registered public key must be Base64 encoded.",
                    exception
            );
        }

        return executeRecorded(
                "ZKP_PROOF_VERIFICATION",
                ZKP_ALGORITHM,
                proof == null ? null : hash(
                        HexFormat.of().formatHex(proof.publicKey())
                        + HexFormat.of().formatHex(proof.commitment())
                ),
                registeredKey,
                () -> zkpEngine.verifyProof(
                        proof,
                        registeredPublicKey
                ),
                result -> Boolean.toString(result),
                result -> result
                        ? "ZKP-VERIFICATION-VALID"
                        : "ZKP-VERIFICATION-INVALID"
        );
    }

    // --------------------------------------------------
    // OPERATION RECORDING
    // --------------------------------------------------

    private <T> T executeRecorded(
            String operationType,
            String algorithm,
            String inputHash,
            OrganizationCryptographicKey key,
            Supplier<T> execution,
            Function<T, String> outputFingerprint,
            Function<T, String> resultReference) {

        Long organizationId =
                currentUserService.getCurrentOrganizationId();

        Long userId =
                currentUserService.getCurrentUserId();

        Organization organization =
                organizationRepository.findById(organizationId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Authenticated organization not found."
                                )
                        );

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Authenticated user not found."
                                )
                        );

        String reference =
                "CRYPTO-" + UUID.randomUUID();

        CryptographicOperation operation =
                new CryptographicOperation();

        operation.setOrganization(organization);
        operation.setInitiatedBy(user);
        operation.setOperationType(operationType);
        operation.setOperationReference(reference);
        operation.setAlgorithm(algorithm);
        operation.setKey(key);
        operation.setInputHash(inputHash);
        operation.setOperationStatus("RUNNING");
        operation.setStartedAt(LocalDateTime.now());

        // Save the operation before executing the cryptographic work.
        operationRepository.save(operation);

        try {

            T result = execution.get();

            String fingerprint = outputFingerprint.apply(result);

            operation.setOutputHash(
                    fingerprint == null ? null : hash(fingerprint)
            );

            operation.setResultReference(
                    reference + ":" + resultReference.apply(result)
            );

            operation.setOperationStatus("COMPLETED");
            operation.setCompletedAt(LocalDateTime.now());

            operationRepository.save(operation);

            return result;

        } catch (RuntimeException exception) {

            operation.setOperationStatus("FAILED");
            operation.setErrorCode(
                    exception.getClass().getSimpleName()
            );
            operation.setCompletedAt(LocalDateTime.now());

            operationRepository.save(operation);

            throw exception;
        }
    }

    private String hash(String value) {

        if (value == null) {
            return null;
        }

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] result = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(result);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 is unavailable.",
                    exception
            );
        }
    }
}
