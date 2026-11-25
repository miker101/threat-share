
package com.example.demo.he;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.crypto.he.HeCryptoService;
import com.example.demo.crypto.he.HeCryptoService.EncryptedValue;
import com.example.demo.crypto.he.HeCryptoService.PaillierKeyPair;

class HeCryptoServiceTest {

    private HeCryptoService heCryptoService;
    private PaillierKeyPair keyPair;

    @BeforeEach
    void setUp() {
        heCryptoService = new HeCryptoService();
        keyPair = heCryptoService.generateKeyPair();
    }

    @Test
    void shouldGenerateKeyPair() {
        assertNotNull(keyPair.getPublicKey());
        assertNotNull(keyPair.getPrivateKey());

        assertEquals(
                keyPair.getPublicKey().getKeyId(),
                keyPair.getPrivateKey().getKeyId()
        );
    }

    @Test
    void shouldEncryptAndDecryptValue() {

        BigInteger original = BigInteger.valueOf(150);

        EncryptedValue encrypted = heCryptoService.encrypt(
                original,
                keyPair.getPublicKey()
        );

        BigInteger decrypted = heCryptoService.decrypt(
                encrypted,
                keyPair.getPrivateKey()
        );

        assertEquals(original, decrypted);
    }

    @Test
    void shouldAddEncryptedValuesWithoutDecrypting() {

        BigInteger valueA = BigInteger.valueOf(100);
        BigInteger valueB = BigInteger.valueOf(200);
        BigInteger valueC = BigInteger.valueOf(300);

        // Three organizations independently encrypt their values.
        EncryptedValue encryptedA = heCryptoService.encrypt(
                valueA, keyPair.getPublicKey()
        );

        EncryptedValue encryptedB = heCryptoService.encrypt(
                valueB, keyPair.getPublicKey()
        );

        EncryptedValue encryptedC = heCryptoService.encrypt(
                valueC, keyPair.getPublicKey()
        );

        // Combine ciphertexts without decrypting individual values.
        EncryptedValue combined = heCryptoService.addEncrypted(
                encryptedA,
                encryptedB,
                keyPair.getPublicKey()
        );

        combined = heCryptoService.addEncrypted(
                combined,
                encryptedC,
                keyPair.getPublicKey()
        );

        BigInteger result = heCryptoService.decrypt(
                combined,
                keyPair.getPrivateKey()
        );

        assertEquals(BigInteger.valueOf(600), result);
    }

    @Test
    void shouldRejectNegativePlaintext() {

        assertThrows(
                IllegalArgumentException.class,
                () -> heCryptoService.encrypt(
                        BigInteger.valueOf(-10),
                        keyPair.getPublicKey()
                )
        );
    }

    @Test
    void shouldRejectCiphertextFromDifferentKeyPair() {

        PaillierKeyPair anotherKeyPair =
                heCryptoService.generateKeyPair();

        EncryptedValue encrypted = heCryptoService.encrypt(
                BigInteger.valueOf(100),
                keyPair.getPublicKey()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> heCryptoService.decrypt(
                        encrypted,
                        anotherKeyPair.getPrivateKey()
                )
        );
    }
}
