
package com.example.demo.crypto.psi;

import java.math.BigInteger;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;
import org.springframework.stereotype.Service;

/**
 * Main entry point for the PSI cryptographic engine.
 *
 * This class coordinates the cryptographic operations.
 * It does not access database records or expose organization secrets.
 */
@Service
public class PsiEngine {

    private final PsiCryptoService cryptoService;
    private final PsiProtocol psiProtocol;

    public PsiEngine(
            PsiCryptoService cryptoService,
            PsiProtocol psiProtocol) {

        this.cryptoService = cryptoService;
        this.psiProtocol = psiProtocol;
    }

    /**
     * Generates a private scalar for one PSI participant.
     *
     * The returned value must remain private to that participant.
     */
    public BigInteger generateParticipantSecret() {
        return cryptoService.generateSecretScalar();
    }

    /**
     * Generates the participant's public elliptic-curve point.
     */
    public byte[] generateParticipantPublicKey(
            BigInteger participantSecret) {

        ECPoint publicPoint =
                cryptoService.generatePublicPoint(participantSecret);

        return cryptoService.encodePoint(publicPoint);
    }

    /**
     * Applies the participant's secret scalar to an encoded input set.
     */
    public List<byte[]> blindInputSet(
            List<byte[]> encodedInputSet,
            BigInteger participantSecret) {

        return psiProtocol.blindSet(
                encodedInputSet,
                participantSecret
        );
    }

    /**
     * Applies a second participant's secret scalar to
     * points received from another participant.
     */
    public List<byte[]> reblindReceivedSet(
            List<byte[]> receivedPoints,
            BigInteger participantSecret) {

        return psiProtocol.reblindSet(
                receivedPoints,
                participantSecret
        );
    }

    /**
     * Calculates the intersection of two double-blinded sets.
     *
     * The participant performing this operation learns the
     * matching encoded points. It does not automatically learn
     * the original indicator values.
     */
    public List<byte[]> calculateIntersection(
            List<byte[]> organizationADoubleBlinded,
            List<byte[]> organizationBDoubleBlinded) {

        return psiProtocol.calculateIntersection(
                organizationADoubleBlinded,
                organizationBDoubleBlinded
        );
    }

    public String getProtocolName() {
        return "Multi-Party Commutative Elliptic-Curve PSI";
    }

    public String getCurveName() {
        return cryptoService.getCurveName();
    }

/**
 * Calculates the common intersection across multiple
 * fully blinded participant datasets.
 */
public List<byte[]> calculateMultiPartyIntersection(
        List<List<byte[]>> fullyBlindedParticipantSets) {

    return psiProtocol.calculateMultiPartyIntersection(
            fullyBlindedParticipantSets
    );
}

/**
 * Applies one participant's secret scalar to all
 * participant datasets in a multi-party PSI round.
 */
public List<List<byte[]>> blindAllParticipantSets(
        List<List<byte[]>> participantSets,
        BigInteger participantSecret) {

    return psiProtocol.blindAllParticipantSets(
            participantSets,
            participantSecret
    );
}


}
