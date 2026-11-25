
package com.example.demo.crypto.zkp;

import java.math.BigInteger;

import org.springframework.stereotype.Component;

import com.example.demo.crypto.zkp.ZkpProofService.ZkpProof;

@Component
public class ZkpEngine {

    private final ZkpProofService proofService;
    private final ZkpVerificationService verificationService;

    public ZkpEngine(
            ZkpProofService proofService,
            ZkpVerificationService verificationService) {

        this.proofService = proofService;
        this.verificationService = verificationService;
    }

    /**
     * Generates a zero-knowledge proof of private-key possession.
     */
    public ZkpProof generateProof(
            BigInteger privateKey,
            String context) {

        return proofService.generateProof(privateKey, context);
    }

    /**
     * Derives the public key corresponding to a private scalar.
     *
     * Used when registering or testing a proof identity.
     */
    public byte[] generatePublicKey(BigInteger privateKey) {
        return proofService.generatePublicKey(privateKey);
    }

    /**
     * Independently verifies a proof.
     */
    public boolean verifyProof(
            ZkpProof proof,
            byte[] registeredPublicKey) {

        return verificationService.verify(
                proof,
                registeredPublicKey
        );
    }
}
