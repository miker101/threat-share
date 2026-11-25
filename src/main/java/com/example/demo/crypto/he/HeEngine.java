
package com.example.demo.crypto.he;

import java.math.BigInteger;

import org.springframework.stereotype.Component;

import com.example.demo.crypto.he.HeCryptoService.EncryptedValue;
import com.example.demo.crypto.he.HeCryptoService.PaillierKeyPair;
import com.example.demo.crypto.he.HeCryptoService.PaillierPublicKey;

@Component
public class HeEngine {

    private final HeCryptoService heCryptoService;

    public HeEngine(HeCryptoService heCryptoService) {
        this.heCryptoService = heCryptoService;
    }

    /**
     * Generates a Paillier key pair.
     */
    public PaillierKeyPair generateKeys() {
        return heCryptoService.generateKeyPair();
    }

    /**
     * Encrypts an organization's numerical contribution.
     */
    public EncryptedValue encrypt(
            BigInteger value,
            PaillierPublicKey publicKey) {

        return heCryptoService.encrypt(value, publicKey);
    }

    /**
     * Combines two encrypted contributions.
     */
    public EncryptedValue aggregate(
            EncryptedValue first,
            EncryptedValue second,
            PaillierPublicKey publicKey) {

        return heCryptoService.addEncrypted(
                first,
                second,
                publicKey
        );
    }

    /**
     * Decrypts an authorized aggregate.
     */
    public BigInteger decrypt(
            EncryptedValue encryptedValue,
            HeCryptoService.PaillierPrivateKey privateKey) {

        return heCryptoService.decrypt(
                encryptedValue,
                privateKey
        );
    }
}
