
package com.example.demo.crypto.psi;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

import org.springframework.stereotype.Service;

@Service
public class PsiCryptoService {

    /*
     * P-256 elliptic curve parameters.
     *
     * Both PSI participants must use exactly the same curve.
     */
    private static final X9ECParameters CURVE_PARAMETERS =
            loadCurveParameters();

    private static final ECCurve CURVE =
            CURVE_PARAMETERS.getCurve();

    private static final BigInteger CURVE_ORDER =
            CURVE_PARAMETERS.getN();

    private static final ECPoint GENERATOR =
            CURVE_PARAMETERS.getG();

    /*
     * Cryptographically secure random number generator.
     */
    private final SecureRandom secureRandom = new SecureRandom();

    private static X9ECParameters loadCurveParameters() {

        X9ECParameters parameters =
                CustomNamedCurves.getByName("secp256r1");

        if (parameters == null) {
            throw new IllegalStateException(
                    "Unable to load secp256r1 curve parameters");
        }

        return parameters;
    }

    /*
     * Generate a fresh random scalar for a PSI participant.
     *
     * The scalar must be kept secret and must never be stored
     * in a public PSI operation record.
     */
    public BigInteger generateSecretScalar() {

        return BigIntegers.createRandomInRange(
                BigInteger.ONE,
                CURVE_ORDER.subtract(BigInteger.ONE),
                secureRandom
        );
    }

    /*
     * Multiply the curve generator by a secret scalar.
     *
     * This produces the participant's public curve point.
     */
    public ECPoint generatePublicPoint(BigInteger secretScalar) {

        validateScalar(secretScalar);

        ECPoint publicPoint =
                GENERATOR.multiply(secretScalar).normalize();

        if (publicPoint.isInfinity() || !publicPoint.isValid()) {
            throw new IllegalStateException(
                    "Generated public point is invalid");
        }

        return publicPoint;
    }

    /*
     * Multiply an existing curve point by a secret scalar.
     *
     * This operation is the foundation of ECDH blinding.
     */
    public ECPoint multiplyPoint(
            ECPoint point,
            BigInteger secretScalar) {

        validateScalar(secretScalar);

        validatePoint(point);

        ECPoint result =
                point.multiply(secretScalar).normalize();

        if (result.isInfinity() || !result.isValid()) {
            throw new IllegalArgumentException(
                    "Point multiplication produced an invalid result");
        }

        return result;
    }

    /*
     * Validate a secret scalar.
     *
     * Valid range: 1 <= scalar < curve order.
     */
    public void validateScalar(BigInteger scalar) {

        if (scalar == null) {
            throw new IllegalArgumentException(
                    "Secret scalar cannot be null");
        }

        if (scalar.signum() <= 0 ||
                scalar.compareTo(CURVE_ORDER) >= 0) {

            throw new IllegalArgumentException(
                    "Secret scalar is outside the valid curve range");
        }
    }

    /*
     * Validate an elliptic-curve point.
     */
    public void validatePoint(ECPoint point) {

        if (point == null) {
            throw new IllegalArgumentException(
                    "Curve point cannot be null");
        }

        if (point.isInfinity()) {
            throw new IllegalArgumentException(
                    "Point at infinity is not accepted");
        }

        if (!point.isValid()) {
            throw new IllegalArgumentException(
                    "Invalid elliptic-curve point");
        }

        /*
         * Confirm that the point belongs to the expected
         * prime-order subgroup.
         */
        if (!point.multiply(CURVE_ORDER).isInfinity()) {
            throw new IllegalArgumentException(
                    "Point does not belong to the expected subgroup");
        }
    }

    /*
     * Encode a curve point using compressed point encoding.
     */
    public byte[] encodePoint(ECPoint point) {

        validatePoint(point);

        return point.normalize().getEncoded(true);
    }

    /*
     * Decode and validate an externally received curve point.
     *
     * Only canonical compressed P-256 points are accepted.
     */
    public ECPoint decodeAndValidatePoint(byte[] encodedPoint) {

        if (encodedPoint == null || encodedPoint.length != 33) {
            throw new IllegalArgumentException(
                    "Invalid compressed P-256 point length");
        }

        if (encodedPoint[0] != 0x02 &&
                encodedPoint[0] != 0x03) {

            throw new IllegalArgumentException(
                    "Only compressed curve points are accepted");
        }

        try {

            ECPoint decodedPoint =
                    CURVE.decodePoint(encodedPoint).normalize();

            validatePoint(decodedPoint);

            byte[] canonicalEncoding =
                    decodedPoint.getEncoded(true);

            if (!MessageDigest.isEqual(
                    encodedPoint,
                    canonicalEncoding)) {

                throw new IllegalArgumentException(
                        "Non-canonical curve point encoding");
            }

            return decodedPoint;

        } catch (IllegalArgumentException exception) {

            throw exception;

        } catch (RuntimeException exception) {

            throw new IllegalArgumentException(
                    "Unable to decode curve point",
                    exception);
        }
    }

    /*
     * Return the curve order.
     *
     * Useful for protocol-level scalar validation and tests.
     */
    public BigInteger getCurveOrder() {
        return CURVE_ORDER;
    }

    /*
     * Return the curve name used by this PSI implementation.
     */
    public String getCurveName() {
        return "secp256r1";
    }
}
