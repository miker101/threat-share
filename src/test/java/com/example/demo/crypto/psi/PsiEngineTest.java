
package com.example.demo.crypto.psi;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.bouncycastle.math.ec.ECPoint;

class PsiEngineTest {

    private PsiCryptoService cryptoService;
    private PsiProtocol psiProtocol;
    private PsiEngine psiEngine;
    private PsiHashToCurve hashToCurve;

    @BeforeEach
    void setUp() {
        cryptoService = new PsiCryptoService();
        psiProtocol = new PsiProtocol(cryptoService);
        psiEngine = new PsiEngine(cryptoService, psiProtocol);
        hashToCurve = new PsiHashToCurve(cryptoService);
    }

    @Test
    void shouldGenerateValidParticipantSecret() {

        BigInteger secret =
                psiEngine.generateParticipantSecret();

        assertNotNull(secret);
        cryptoService.validateScalar(secret);
    }

    @Test
    void shouldGenerateValidPublicPoint() {

        BigInteger secret =
                psiEngine.generateParticipantSecret();

        byte[] publicKey =
                psiEngine.generateParticipantPublicKey(secret);

        assertNotNull(publicKey);
        assertEquals(33, publicKey.length);

        ECPoint decoded =
                cryptoService.decodeAndValidatePoint(publicKey);

        assertTrue(decoded.isValid());
        assertFalse(decoded.isInfinity());
    }

    @Test
    void sameIndicatorShouldProduceSamePoint() {

        byte[] first =
                hashToCurve.hashIndicator(
                        "IP_ADDRESS",
                        "192.0.2.10"
                );

        byte[] second =
                hashToCurve.hashIndicator(
                        "IP_ADDRESS",
                        "192.0.2.10"
                );

        assertArrayEquals(first, second);
    }

    @Test
    void differentIndicatorTypesShouldProduceDifferentPoints() {

        byte[] ipPoint =
                hashToCurve.hashIndicator(
                        "IP_ADDRESS",
                        "example.com"
                );

        byte[] domainPoint =
                hashToCurve.hashIndicator(
                        "DOMAIN",
                        "example.com"
                );

        assertFalse(
                java.util.Arrays.equals(ipPoint, domainPoint)
        );
    }

    @Test
    void twoParticipantsShouldFindTheirIntersection() {

        BigInteger secretA =
                psiEngine.generateParticipantSecret();

        BigInteger secretB =
                psiEngine.generateParticipantSecret();

        byte[] commonIndicator =
                hashToCurve.hashIndicator(
                        "FILE_HASH",
                        "sha256-example-common"
                );

        byte[] indicatorA =
                hashToCurve.hashIndicator(
                        "IP_ADDRESS",
                        "192.0.2.10"
                );

        byte[] indicatorB =
                hashToCurve.hashIndicator(
                        "IP_ADDRESS",
                        "198.51.100.20"
                );

        List<byte[]> setA = List.of(
                commonIndicator,
                indicatorA
        );

        List<byte[]> setB = List.of(
                commonIndicator,
                indicatorB
        );

        // Organization A blinds its own set.
        List<byte[]> aBlinded =
                psiEngine.blindInputSet(setA, secretA);

        // Organization B blinds A's points.
        List<byte[]> aDoubleBlinded =
                psiEngine.reblindReceivedSet(
                        aBlinded,
                        secretB
                );

        // Organization B blinds its own set.
        List<byte[]> bBlinded =
                psiEngine.blindInputSet(setB, secretB);

        // Organization A reblinds B's points.
        List<byte[]> bDoubleBlinded =
                psiEngine.reblindReceivedSet(
                        bBlinded,
                        secretA
                );

        // B calculates the intersection.
        List<byte[]> intersection =
                psiEngine.calculateIntersection(
                        aDoubleBlinded,
                        bDoubleBlinded
                );

        assertEquals(1, intersection.size());

        assertArrayEquals(
                aDoubleBlinded.get(0),
                intersection.get(0)
        );
    }

    @Test
    void shouldRejectInvalidSecretScalar() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cryptoService.validateScalar(BigInteger.ZERO)
        );
    }

    @Test
    void shouldRejectInvalidEncodedPoint() {

        byte[] invalidPoint = new byte[33];

        assertThrows(
                IllegalArgumentException.class,
                () -> cryptoService.decodeAndValidatePoint(
                        invalidPoint
                )
        );
    }
}
