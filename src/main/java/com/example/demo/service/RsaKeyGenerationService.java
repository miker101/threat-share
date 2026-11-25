
package com.example.demo.service;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import org.springframework.stereotype.Service;

import org.bouncycastle.cert.CertIOException;

@Service
public class RsaKeyGenerationService {

    private static final int RSA_KEY_SIZE = 3072;

    private final SecureRandom secureRandom = new SecureRandom();

    public RsaKeyGenerationService() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public GeneratedRsaKey generateForOrganization(
            Long organizationId,
            int keyVersion
    ) throws GeneralSecurityException,
    OperatorCreationException,
    CertIOException {

        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("Invalid organization ID.");
        }

        if (keyVersion <= 0) {
            throw new IllegalArgumentException("Invalid key version.");
        }

        // Generate RSA-3072 key pair.
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(RSA_KEY_SIZE, secureRandom);

        KeyPair keyPair = generator.generateKeyPair();

        // Create a self-signed certificate for PKCS#12 storage.
        X500Name subject = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(
                        BCStyle.CN,
                        "ThreatShare Organization " + organizationId
                                + " Signing Key v" + keyVersion
                )
                .build();

        Instant now = Instant.now();

        Date notBefore = Date.from(now.minus(5, ChronoUnit.MINUTES));
        Date notAfter = Date.from(now.plus(3650, ChronoUnit.DAYS));

        BigInteger serialNumber =
                new BigInteger(160, secureRandom).add(BigInteger.ONE);

        X509v3CertificateBuilder certificateBuilder =
                new JcaX509v3CertificateBuilder(
                        subject,
                        serialNumber,
                        notBefore,
                        notAfter,
                        subject,
                        keyPair.getPublic()
                );

        certificateBuilder.addExtension(
                Extension.basicConstraints,
                true,
                new BasicConstraints(false)
        );

        certificateBuilder.addExtension(
                Extension.keyUsage,
                true,
                new KeyUsage(KeyUsage.digitalSignature)
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());

        X509Certificate certificate =
                new JcaX509CertificateConverter()
                        .setProvider("BC")
                        .getCertificate(
                                certificateBuilder.build(signer)
                        );

        // Verify that the certificate corresponds to the generated key.
        certificate.verify(keyPair.getPublic());
        certificate.checkValidity(new Date());

        return new GeneratedRsaKey(keyPair, certificate);
    }

    public record GeneratedRsaKey(
            KeyPair keyPair,
            X509Certificate certificate
    ) {}
}
