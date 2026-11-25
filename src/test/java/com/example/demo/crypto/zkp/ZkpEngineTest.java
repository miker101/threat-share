
package com.example.demo.crypto.zkp;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.crypto.zkp.ZkpProofService.ZkpProof;

class ZkpEngineTest {

    private ZkpEngine engine;

    private BigInteger privateKey;
    private byte[] registeredPublicKey;

    @BeforeEach
    void setUp() {
        engine = new ZkpEngine(
                new ZkpProofService(),
                new ZkpVerificationService()
        );

        privateKey = BigInteger.valueOf(123456789L);

        registeredPublicKey =
                engine.generatePublicKey(privateKey);
    }

    @Test
    void shouldGenerateAndVerifyValidProof() {

        ZkpProof proof = engine.generateProof(
                privateKey,
                "THREATSHARE-ORG-REGISTRATION-1001"
        );

        assertTrue(engine.verifyProof(
                proof,
                registeredPublicKey
        ));
    }

    @Test
    void shouldRejectModifiedResponse() {

        ZkpProof original = engine.generateProof(
                privateKey,
                "THREATSHARE-ORG-REGISTRATION-1001"
        );

        ZkpProof tampered = new ZkpProof(
                original.publicKey(),
                original.commitment(),
                original.response().add(BigInteger.ONE),
                original.context(),
                original.protocol()
        );

        assertFalse(engine.verifyProof(
                tampered,
                registeredPublicKey
        ));
    }

    @Test
    void shouldRejectModifiedContext() {

        ZkpProof original = engine.generateProof(
                privateKey,
                "THREATSHARE-ORG-REGISTRATION-1001"
        );

        ZkpProof tampered = new ZkpProof(
                original.publicKey(),
                original.commitment(),
                original.response(),
                "DIFFERENT-ORGANIZATION",
                original.protocol()
        );

        assertFalse(engine.verifyProof(
                tampered,
                registeredPublicKey
        ));
    }

    @Test
    void shouldRejectDifferentRegisteredPublicKey() {

        ZkpProof proof = engine.generateProof(
                privateKey,
                "THREATSHARE-ORG-REGISTRATION-1001"
        );

        BigInteger anotherPrivateKey =
                BigInteger.valueOf(987654321L);

        byte[] anotherPublicKey =
                engine.generatePublicKey(anotherPrivateKey);

        assertFalse(engine.verifyProof(
                proof,
                anotherPublicKey
        ));
    }

    @Test
    void shouldRejectInvalidPublicKeyEncoding() {

        ZkpProof proof = engine.generateProof(
                privateKey,
                "THREATSHARE-ORG-REGISTRATION-1001"
        );

        byte[] invalidPublicKey = new byte[65];

        ZkpProof tampered = new ZkpProof(
                invalidPublicKey,
                proof.commitment(),
                proof.response(),
                proof.context(),
                proof.protocol()
        );

        assertFalse(engine.verifyProof(
                tampered,
                registeredPublicKey
        ));
    }

    @Test
    void shouldRejectNullProof() {
        assertFalse(engine.verifyProof(null, registeredPublicKey));
    }
}
