
package com.example.demo.crypto.psi;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.crypto.hash2curve.HashToCurveProfile;
import org.bouncycastle.crypto.hash2curve.HashToEllipticCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.stereotype.Service;

@Service
public class PsiHashToCurve {

    private static final String DOMAIN_SEPARATOR =
            "THREATSHARE-PSI-V1-P256_XMD:SHA-256_SSWU_RO_";

    private final PsiCryptoService cryptoService;

    private final HashToEllipticCurve hashToCurve;

    public PsiHashToCurve(PsiCryptoService cryptoService) {
        this.cryptoService = cryptoService;

        this.hashToCurve = HashToEllipticCurve.getInstance(
                HashToCurveProfile.P256_XMD_SHA_256,
                DOMAIN_SEPARATOR
        );
    }

    /**
     * Maps a canonical threat indicator to a P-256 point.
     *
     * Examples of indicator types:
     * IP_ADDRESS, DOMAIN, FILE_HASH, URL
     *
     * The value must be normalized before being passed here.
     */
    public byte[] hashIndicator(
            String indicatorType,
            String canonicalValue) {

        if (indicatorType == null || indicatorType.isBlank()) {
            throw new IllegalArgumentException(
                    "Indicator type is required");
        }

        if (canonicalValue == null || canonicalValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Indicator value is required");
        }

        String normalizedType =
                indicatorType.trim().toUpperCase();

        String message =
                "THREATSHARE-INDICATOR-V1|"
                + normalizedType
                + "|"
                + canonicalValue;

        byte[] messageBytes =
                message.getBytes(StandardCharsets.UTF_8);

        ECPoint point =
                hashToCurve.hashToCurve(messageBytes).normalize();

        if (point.isInfinity() || !point.isValid()) {
            throw new IllegalStateException(
                    "Hash-to-curve generated an invalid point");
        }

        return cryptoService.encodePoint(point);
    }

    public String getSuiteName() {
        return "P256_XMD:SHA-256_SSWU_RO_";
    }
}
