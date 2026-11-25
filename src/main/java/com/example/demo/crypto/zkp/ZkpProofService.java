
package com.example.demo.crypto.zkp;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class ZkpProofService {

    static final BigInteger P = new BigInteger(
        "FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16);

    static final BigInteger A = P.subtract(BigInteger.valueOf(3));

    static final BigInteger B = new BigInteger(
        "5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16);

    static final BigInteger N = new BigInteger(
        "FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);

    static final Point G = new Point(
        new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16),
        new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)
    );

    static final String DOMAIN =
        "THREATSHARE-SCHNORR-P256-FIAT-SHAMIR-V1";

    private static final SecureRandom RANDOM = new SecureRandom();

    public ZkpProof generateProof(
            BigInteger privateKey,
            String context) {

        validatePrivateKey(privateKey);
        validateContext(context);

        Point publicKey = multiply(G, privateKey);

        BigInteger nonce = randomScalar();
        Point commitment = multiply(G, nonce);

        BigInteger challenge = challenge(
            publicKey, commitment, context
        );

        BigInteger response = nonce
            .add(challenge.multiply(privateKey))
            .mod(N);

        return new ZkpProof(
            encode(publicKey),
            encode(commitment),
            response,
            context,
            DOMAIN
        );
    }

    public byte[] generatePublicKey(BigInteger privateKey) {
        validatePrivateKey(privateKey);
        return encode(multiply(G, privateKey));
    }

    static BigInteger challenge(
            Point publicKey,
            Point commitment,
            String context) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            update(digest, DOMAIN.getBytes(StandardCharsets.UTF_8));
            update(digest, context.getBytes(StandardCharsets.UTF_8));
            update(digest, encode(publicKey));
            update(digest, encode(commitment));

            return new BigInteger(1, digest.digest()).mod(N);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static void update(MessageDigest digest, byte[] data) {
        digest.update(ByteBuffer.allocate(4).putInt(data.length).array());
        digest.update(data);
    }

    static BigInteger randomScalar() {
        BigInteger value;

        do {
            value = new BigInteger(N.bitLength(), RANDOM);
        } while (value.signum() == 0 || value.compareTo(N) >= 0);

        return value;
    }

    static Point multiply(Point point, BigInteger scalar) {
        Point result = null;
        Point current = point;

        for (int i = 0; i < scalar.bitLength(); i++) {
            if (scalar.testBit(i)) {
                result = add(result, current);
            }
            current = add(current, current);
        }

        return result;
    }

    static Point add(Point p1, Point p2) {
        if (p1 == null) return p2;
        if (p2 == null) return p1;

        if (p1.x.equals(p2.x) &&
            p1.y.add(p2.y).mod(P).equals(BigInteger.ZERO)) {
            return null;
        }

        BigInteger slope;

        if (p1.equals(p2)) {
            BigInteger numerator = BigInteger.valueOf(3)
                .multiply(p1.x.multiply(p1.x))
                .add(A).mod(P);

            BigInteger denominator =
                p1.y.multiply(BigInteger.TWO).mod(P);

            if (denominator.signum() == 0) return null;

            slope = numerator.multiply(
                denominator.modInverse(P)
            ).mod(P);

        } else {
            BigInteger numerator =
                p2.y.subtract(p1.y).mod(P);

            BigInteger denominator =
                p2.x.subtract(p1.x).mod(P);

            slope = numerator.multiply(
                denominator.modInverse(P)
            ).mod(P);
        }

        BigInteger x3 = slope.multiply(slope)
            .subtract(p1.x).subtract(p2.x).mod(P);

        BigInteger y3 = slope.multiply(p1.x.subtract(x3))
            .subtract(p1.y).mod(P);

        return new Point(x3, y3);
    }

    static byte[] encode(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("Point at infinity");
        }

        byte[] result = new byte[65];
        result[0] = 0x04;

        copy32(point.x, result, 1);
        copy32(point.y, result, 33);

        return result;
    }

    static Point decode(byte[] encoded) {
        if (encoded == null || encoded.length != 65 ||
            encoded[0] != 0x04) {
            throw new IllegalArgumentException("Invalid EC point encoding");
        }

        BigInteger x = new BigInteger(1, slice(encoded, 1));
        BigInteger y = new BigInteger(1, slice(encoded, 33));

        if (x.compareTo(P) >= 0 || y.compareTo(P) >= 0) {
            throw new IllegalArgumentException("Invalid EC coordinates");
        }

        Point point = new Point(x, y);

        BigInteger left = y.multiply(y).mod(P);

        BigInteger right = x.multiply(x).multiply(x)
            .add(A.multiply(x)).add(B).mod(P);

        if (!left.equals(right)) {
            throw new IllegalArgumentException("Point is not on P-256");
        }

        return point;
    }

    private static byte[] slice(byte[] input, int start) {
        byte[] output = new byte[32];
        System.arraycopy(input, start, output, 0, 32);
        return output;
    }

    private static void copy32(BigInteger value, byte[] target, int offset) {
        byte[] bytes = value.toByteArray();
        int sourceOffset = bytes.length > 32 ? bytes.length - 32 : 0;
        int length = Math.min(bytes.length, 32);

        System.arraycopy(
            bytes, sourceOffset, target, offset + 32 - length, length
        );
    }

    private static void validatePrivateKey(BigInteger key) {
        if (key == null || key.signum() <= 0 || key.compareTo(N) >= 0) {
            throw new IllegalArgumentException("Invalid private scalar");
        }
    }

    private static void validateContext(String context) {
        if (context == null || context.isBlank() ||
            context.length() > 1000) {
            throw new IllegalArgumentException("Invalid proof context");
        }
    }

    static final class Point {
        final BigInteger x;
        final BigInteger y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Point point)) return false;
            return x.equals(point.x) && y.equals(point.y);
        }

        @Override
        public int hashCode() {
            return x.hashCode() * 31 + y.hashCode();
        }
    }

    public record ZkpProof(
        byte[] publicKey,
        byte[] commitment,
        BigInteger response,
        String context,
        String protocol
    ) {
        public ZkpProof {
            publicKey = publicKey.clone();
            commitment = commitment.clone();
        }

        @Override
        public byte[] publicKey() {
            return publicKey.clone();
        }

        @Override
        public byte[] commitment() {
            return commitment.clone();
        }
    }
}
