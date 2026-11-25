
package com.example.demo.crypto.he;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.springframework.stereotype.Service;

@Service
public class HeCryptoService {

    private static final int KEY_SIZE_BITS = 2048;

    private static final BigInteger ONE = BigInteger.ONE;
    private static final BigInteger TWO = BigInteger.TWO;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a Paillier public/private key pair.
     */
    public PaillierKeyPair generateKeyPair() {

        BigInteger p;
        BigInteger q;
        BigInteger n;

        // Ensure the resulting modulus is exactly 2048 bits.
        do {
            p = BigInteger.probablePrime(KEY_SIZE_BITS / 2, random);
            q = BigInteger.probablePrime(KEY_SIZE_BITS / 2, random);
            n = p.multiply(q);
        } while (p.equals(q) || n.bitLength() != KEY_SIZE_BITS);

        BigInteger nSquared = n.multiply(n);

        BigInteger pMinusOne = p.subtract(ONE);
        BigInteger qMinusOne = q.subtract(ONE);

        // lambda = lcm(p - 1, q - 1)
        BigInteger lambda = pMinusOne.divide(
                pMinusOne.gcd(qMinusOne)
        ).multiply(qMinusOne);

        // Standard Paillier generator: g = n + 1
        BigInteger g = n.add(ONE);

        // mu = L(g^lambda mod n²)^-1 mod n
        BigInteger powered = g.modPow(lambda, nSquared);

        BigInteger lValue = powered.subtract(ONE).divide(n);

        BigInteger mu = lValue.modInverse(n);

        String keyId = generateKeyId(n);

        PaillierPublicKey publicKey =
                new PaillierPublicKey(n, g, nSquared, keyId);

        PaillierPrivateKey privateKey =
                new PaillierPrivateKey(n, lambda, mu, keyId);

        return new PaillierKeyPair(publicKey, privateKey);
    }

    /**
     * Encrypts a non-negative integer.
     */
    public EncryptedValue encrypt(
            BigInteger plaintext,
            PaillierPublicKey publicKey) {

        if (plaintext == null || publicKey == null) {
            throw new IllegalArgumentException(
                    "Plaintext and public key are required."
            );
        }

        BigInteger n = publicKey.n;

        if (plaintext.signum() < 0 || plaintext.compareTo(n) >= 0) {
            throw new IllegalArgumentException(
                    "Plaintext must be in the range 0 <= m < n."
            );
        }

        // Select random r where 1 <= r < n and gcd(r,n) = 1.
        BigInteger r;

        do {
            r = new BigInteger(n.bitLength(), random);
        } while (
                r.signum() == 0 ||
                r.compareTo(n) >= 0 ||
                !r.gcd(n).equals(ONE)
        );

        // c = g^m * r^n mod n²
        BigInteger first = publicKey.g.modPow(
                plaintext,
                publicKey.nSquared
        );

        BigInteger second = r.modPow(
                n,
                publicKey.nSquared
        );

        BigInteger ciphertext = first.multiply(second)
                .mod(publicKey.nSquared);

        return new EncryptedValue(ciphertext, publicKey.keyId);
    }

    /**
     * Adds two ciphertexts without decrypting them.
     *
     * Both ciphertexts must belong to the same public key.
     */
    public EncryptedValue addEncrypted(
            EncryptedValue ciphertextA,
            EncryptedValue ciphertextB,
            PaillierPublicKey publicKey) {

        if (ciphertextA == null ||
                ciphertextB == null ||
                publicKey == null) {

            throw new IllegalArgumentException(
                    "Both ciphertexts and the public key are required."
            );
        }

        validateCiphertext(ciphertextA, publicKey);
        validateCiphertext(ciphertextB, publicKey);

        // E(a + b) = E(a) * E(b) mod n²
        BigInteger result = ciphertextA.value
                .multiply(ciphertextB.value)
                .mod(publicKey.nSquared);

        return new EncryptedValue(result, publicKey.keyId);
    }

    /**
     * Decrypts an encrypted value using the private key.
     */
    public BigInteger decrypt(
            EncryptedValue encryptedValue,
            PaillierPrivateKey privateKey) {

        if (encryptedValue == null || privateKey == null) {
            throw new IllegalArgumentException(
                    "Ciphertext and private key are required."
            );
        }

        if (!privateKey.keyId.equals(encryptedValue.keyId)) {
            throw new IllegalArgumentException(
                    "Ciphertext does not belong to this key pair."
            );
        }

        BigInteger n = privateKey.n;
        BigInteger nSquared = n.multiply(n);
        BigInteger c = encryptedValue.value;

        if (c.signum() <= 0 ||
                c.compareTo(nSquared) >= 0 ||
                !c.gcd(n).equals(ONE)) {

            throw new IllegalArgumentException(
                    "Invalid Paillier ciphertext."
            );
        }

        // u = c^lambda mod n²
        BigInteger u = c.modPow(
                privateKey.lambda,
                nSquared
        );

        // L(u) = (u - 1) / n
        BigInteger lValue = u.subtract(ONE).divide(n);

        // m = L(u) * mu mod n
        return lValue.multiply(privateKey.mu).mod(n);
    }

    private void validateCiphertext(
            EncryptedValue ciphertext,
            PaillierPublicKey publicKey) {

        if (!publicKey.keyId.equals(ciphertext.keyId)) {
            throw new IllegalArgumentException(
                    "Ciphertexts must use the same public key."
            );
        }

        BigInteger c = ciphertext.value;

        if (c.signum() <= 0 ||
                c.compareTo(publicKey.nSquared) >= 0 ||
                !c.gcd(publicKey.n).equals(ONE)) {

            throw new IllegalArgumentException(
                    "Invalid Paillier ciphertext."
            );
        }
    }

    private String generateKeyId(BigInteger n) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(n.toByteArray());

            return java.util.HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable.",
                    exception
            );
        }
    }

    // -------------------------------------------------
    // KEY AND CIPHERTEXT TYPES
    // -------------------------------------------------

    public static final class PaillierKeyPair {

        private final PaillierPublicKey publicKey;
        private final PaillierPrivateKey privateKey;

        private PaillierKeyPair(
                PaillierPublicKey publicKey,
                PaillierPrivateKey privateKey) {

            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public PaillierPublicKey getPublicKey() {
            return publicKey;
        }

        public PaillierPrivateKey getPrivateKey() {
            return privateKey;
        }
    }

    public static final class PaillierPublicKey {

        private final BigInteger n;
        private final BigInteger g;
        private final BigInteger nSquared;
        private final String keyId;

        private PaillierPublicKey(
                BigInteger n,
                BigInteger g,
                BigInteger nSquared,
                String keyId) {

            this.n = n;
            this.g = g;
            this.nSquared = nSquared;
            this.keyId = keyId;
        }

        public String getKeyId() {
            return keyId;
        }

        public BigInteger getModulus() {
            return n;
        }
    }

    public static final class PaillierPrivateKey {

        private final BigInteger n;
        private final BigInteger lambda;
        private final BigInteger mu;
        private final String keyId;

        private PaillierPrivateKey(
                BigInteger n,
                BigInteger lambda,
                BigInteger mu,
                String keyId) {

            this.n = n;
            this.lambda = lambda;
            this.mu = mu;
            this.keyId = keyId;
        }

        public String getKeyId() {
            return keyId;
        }
    }

    public static final class EncryptedValue {

        private final BigInteger value;
        private final String keyId;

        private EncryptedValue(BigInteger value, String keyId) {
            this.value = value;
            this.keyId = keyId;
        }

        public BigInteger getValue() {
            return value;
        }

        public String getKeyId() {
            return keyId;
        }
    }
}
