
package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "digital_signatures")
public class DigitalSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "threat_id", nullable = false)
    private Threat threat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signer_id", nullable = false)
    private User signer;

    @Column(name = "signature_value", nullable = false, columnDefinition = "TEXT")
    private String signatureValue;

    @Column(name = "signed_content_hash", nullable = false, length = 128)
    private String signedContentHash;

    @Column(name = "signature_algorithm", nullable = false, length = 60)
    private String signatureAlgorithm;

    @Column(name = "hash_algorithm", nullable = false, length = 40)
    private String hashAlgorithm;

    @Column(name = "public_key_reference", length = 255)
    private String publicKeyReference;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "PENDING";

    @Column(name = "signed_at", nullable = false,
            insertable = false, updatable = false)
    private LocalDateTime signedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // Getters and setters

    public Long getId() { return id; }

    public Threat getThreat() { return threat; }
    public void setThreat(Threat threat) { this.threat = threat; }

    public User getSigner() { return signer; }
    public void setSigner(User signer) { this.signer = signer; }

    public String getSignatureValue() { return signatureValue; }
    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }

    public String getSignedContentHash() { return signedContentHash; }
    public void setSignedContentHash(String signedContentHash) {
        this.signedContentHash = signedContentHash;
    }

    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getHashAlgorithm() { return hashAlgorithm; }
    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getPublicKeyReference() { return publicKeyReference; }
    public void setPublicKeyReference(String publicKeyReference) {
        this.publicKeyReference = publicKeyReference;
    }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getSignedAt() { return signedAt; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
