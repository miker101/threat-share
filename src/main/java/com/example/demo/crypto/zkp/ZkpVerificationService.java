
package com.example.demo.crypto.zkp;

import java.math.BigInteger;

import org.springframework.stereotype.Service;

import com.example.demo.crypto.zkp.ZkpProofService.Point;
import com.example.demo.crypto.zkp.ZkpProofService.ZkpProof;

@Service
public class ZkpVerificationService {

    /**
     * Verifies a Schnorr Fiat-Shamir proof.
     *
     * Verification equation:
     * zG = R + cY
     *
     * z  = proof response
     * G  = P-256 generator
     * R  = commitment
     * c  = Fiat-Shamir challenge
     * Y  = public key
     */
	public boolean verify(ZkpProof proof, byte[] registeredPublicKey) {

        if (proof == null) {
            return false;
        }
        if (registeredPublicKey == null ||
                registeredPublicKey.length != 65 ||
                !java.security.MessageDigest.isEqual(
                        proof.publicKey(), registeredPublicKey)) {
            return false;
        }

        try {
            if (!ZkpProofService.DOMAIN.equals(proof.protocol())) {
                return false;
            }

            if (proof.context() == null ||
                    proof.context().isBlank() ||
                    proof.context().length() > 1000) {
                return false;
            }

            BigInteger response = proof.response();

            if (response == null ||
                    response.signum() < 0 ||
                    response.compareTo(ZkpProofService.N) >= 0) {
                return false;
            }

            Point publicKey =
                    ZkpProofService.decode(proof.publicKey());

            Point commitment =
                    ZkpProofService.decode(proof.commitment());

            // Recompute Fiat-Shamir challenge.
            BigInteger challenge = ZkpProofService.challenge(
                    publicKey,
                    commitment,
                    proof.context()
            );

            // Left side: zG
            Point left = ZkpProofService.multiply(
                    ZkpProofService.G,
                    response
            );

            // Right side: R + cY
            Point challengePublicKey =
                    ZkpProofService.multiply(
                            publicKey,
                            challenge
                    );

            Point right = ZkpProofService.add(
                    commitment,
                    challengePublicKey
            );

            if (left == null || right == null) {
                return left == right;
            }

            return left.equals(right);

        } catch (RuntimeException exception) {
            return false;
        }
    }
}
