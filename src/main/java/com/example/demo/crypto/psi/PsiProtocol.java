
package com.example.demo.crypto.psi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.math.ec.ECPoint;
import org.springframework.stereotype.Service;

@Service
public class PsiProtocol {

    private final PsiCryptoService cryptoService;

    public PsiProtocol(PsiCryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /*
     * STAGE 1
     *
     * Organization A blinds its hash-to-curve points.
     *
     * Input:
     * H(A)
     *
     * Output:
     * H(A)^a
     *
     * The secret scalar belongs to Organization A.
     */
    public List<byte[]> blindSet(
            List<byte[]> encodedPoints,
            BigInteger secretScalar) {

        validateInput(encodedPoints);

        cryptoService.validateScalar(secretScalar);

        List<byte[]> blindedPoints = new ArrayList<>();

        for (byte[] encodedPoint : encodedPoints) {

            ECPoint point =
                    cryptoService.decodeAndValidatePoint(encodedPoint);

            ECPoint blindedPoint =
                    cryptoService.multiplyPoint(point, secretScalar);

            blindedPoints.add(
                    cryptoService.encodePoint(blindedPoint)
            );
        }

        return List.copyOf(blindedPoints);
    }

    /*
     * STAGE 2
     *
     * Organization B applies its secret scalar to
     * points received from Organization A.
     *
     * Input:
     * H(A)^a
     *
     * Output:
     * H(A)^(ab)
     */
    public List<byte[]> reblindSet(
            List<byte[]> receivedPoints,
            BigInteger secretScalar) {

        return blindSet(receivedPoints, secretScalar);
    }

    /*
     * STAGE 3
     *
     * Organization B compares its double-blinded
     * dataset with the double-blinded dataset
     * returned by Organization A.
     *
     * Both sets must contain points in the same
     * canonical encoding.
     *
     * The returned values are cryptographic point
     * references, not raw indicator values.
     */
    public List<byte[]> calculateIntersection(
            List<byte[]> organizationADoubleBlinded,
            List<byte[]> organizationBDoubleBlinded) {

        validateInput(organizationADoubleBlinded);
        validateInput(organizationBDoubleBlinded);

        Set<String> organizationASet = new HashSet<>();

        for (byte[] point : organizationADoubleBlinded) {

            ECPoint decoded =
                    cryptoService.decodeAndValidatePoint(point);

            organizationASet.add(
                    Base64.getEncoder().encodeToString(
                            cryptoService.encodePoint(decoded)
                    )
            );
        }

        List<byte[]> intersection = new ArrayList<>();

        Set<String> alreadyAdded = new HashSet<>();

        for (byte[] point : organizationBDoubleBlinded) {

            ECPoint decoded =
                    cryptoService.decodeAndValidatePoint(point);

            byte[] canonicalPoint =
                    cryptoService.encodePoint(decoded);

            String pointReference =
                    Base64.getEncoder().encodeToString(canonicalPoint);

            if (organizationASet.contains(pointReference)
                    && alreadyAdded.add(pointReference)) {

                intersection.add(canonicalPoint);
            }
        }

        return List.copyOf(intersection);
    }

    /*
     * Reject invalid or excessively large protocol inputs.
     */
    private void validateInput(List<byte[]> encodedPoints) {

        if (encodedPoints == null) {
            throw new IllegalArgumentException(
                    "PSI input set cannot be null");
        }

        if (encodedPoints.isEmpty()) {
            throw new IllegalArgumentException(
                    "PSI input set cannot be empty");
        }

        if (encodedPoints.size() > 100_000) {
            throw new IllegalArgumentException(
                    "PSI input set exceeds the permitted size");
        }
    }

/**
 * Calculates the intersection across all participating organizations.
 *
 * Every input set must already have been blinded by every
 * participant's secret scalar exactly once.
 *
 * Requires at least two participating organizations.
 */
public List<byte[]> calculateMultiPartyIntersection(
        List<List<byte[]>> fullyBlindedParticipantSets) {

    if (fullyBlindedParticipantSets == null
            || fullyBlindedParticipantSets.size() < 2) {
        throw new IllegalArgumentException(
                "Multi-party PSI requires at least two organizations.");
    }

    Set<String> commonPoints = null;

    for (List<byte[]> participantSet : fullyBlindedParticipantSets) {

        validateInput(participantSet);

        Set<String> currentPoints = new HashSet<>();

        for (byte[] encodedPoint : participantSet) {

            ECPoint decoded =
                    cryptoService.decodeAndValidatePoint(encodedPoint);

            String reference = Base64.getEncoder().encodeToString(
                    cryptoService.encodePoint(decoded));

            currentPoints.add(reference);
        }

        if (commonPoints == null) {
            commonPoints = currentPoints;
        } else {
            commonPoints.retainAll(currentPoints);
        }

        if (commonPoints.isEmpty()) {
            return List.of();
        }
    }

    List<byte[]> intersection = new ArrayList<>();

    for (String reference : commonPoints) {
        intersection.add(Base64.getDecoder().decode(reference));
    }

    return List.copyOf(intersection);
}

/**
 * Applies one participant's private scalar to every
 * participant dataset in the current PSI round.
 *
 * Each organization must execute this round using its
 * own secret scalar. The scalar must never be transmitted.
 */
public List<List<byte[]>> blindAllParticipantSets(
        List<List<byte[]>> participantSets,
        BigInteger participantSecret) {

    if (participantSets == null || participantSets.size() < 2) {
        throw new IllegalArgumentException(
                "At least two participant datasets are required.");
    }

    cryptoService.validateScalar(participantSecret);

    List<List<byte[]>> blindedSets = new ArrayList<>();

    for (List<byte[]> participantSet : participantSets) {

        List<byte[]> blindedSet =
                blindSet(participantSet, participantSecret);

        blindedSets.add(blindedSet);
    }

    return List.copyOf(blindedSets);
}

}
