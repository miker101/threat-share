
package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_cryptographic_keys")
public class OrganizationCryptographicKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "key_purpose", nullable = false, length = 60)
    private String keyPurpose;

    @Column(name = "key_type", nullable = false, length = 50)
    private String keyType;

    @Column(name = "algorithm", nullable = false, length = 100)
    private String algorithm;

    @Column(name = "key_version", nullable = false)
    private Integer keyVersion;

    @Column(name = "public_key_material", columnDefinition = "MEDIUMTEXT")
    private String publicKeyMaterial;

    @Column(name = "key_format", length = 40)
    private String keyFormat;

    @Column(name = "key_fingerprint", length = 128, unique = true)
    private String keyFingerprint;

    @Column(name = "protected_key_reference", length = 512)
    private String protectedKeyReference;

    @Column(name = "key_status", nullable = false, length = 30)
    private String keyStatus = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Managed by MySQL.
    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String revocationReason;

    public OrganizationCryptographicKey() {
    }

    public Long getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getKeyPurpose() {
        return keyPurpose;
    }

    public void setKeyPurpose(String keyPurpose) {
        this.keyPurpose = keyPurpose;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Integer getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(Integer keyVersion) {
        this.keyVersion = keyVersion;
    }

    public String getPublicKeyMaterial() {
        return publicKeyMaterial;
    }

    public void setPublicKeyMaterial(String publicKeyMaterial) {
        this.publicKeyMaterial = publicKeyMaterial;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    public void setKeyFormat(String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public String getKeyFingerprint() {
        return keyFingerprint;
    }

    public void setKeyFingerprint(String keyFingerprint) {
        this.keyFingerprint = keyFingerprint;
    }

    public String getProtectedKeyReference() {
        return protectedKeyReference;
    }

    public void setProtectedKeyReference(String protectedKeyReference) {
        this.protectedKeyReference = protectedKeyReference;
    }

    public String getKeyStatus() {
        return keyStatus;
    }

    public void setKeyStatus(String keyStatus) {
        this.keyStatus = keyStatus;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }
}
