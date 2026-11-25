
package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "zero_knowledge_proofs")
public class ZeroKnowledgeProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cryptographic_operation_id", nullable = false)
    private CryptographicOperation cryptographicOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "proof_system", nullable = false, length = 100)
    private String proofSystem;

    @Column(name = "proof_version", length = 50)
    private String proofVersion;

    @Column(name = "statement_description", nullable = false, columnDefinition = "TEXT")
    private String statementDescription;

    @Column(name = "public_inputs", columnDefinition = "JSON")
    private String publicInputs;

    @Column(name = "verification_key_reference", length = 512)
    private String verificationKeyReference;

    @Column(name = "proof_reference", nullable = false, length = 512)
    private String proofReference;

    @Column(name = "proof_hash", nullable = false, length = 128)
    private String proofHash;

    @Column(name = "hash_algorithm", nullable = false, length = 40)
    private String hashAlgorithm = "SHA-256";

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CryptographicOperation getCryptographicOperation() {
        return cryptographicOperation;
    }

    public void setCryptographicOperation(CryptographicOperation cryptographicOperation) {
        this.cryptographicOperation = cryptographicOperation;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getProofSystem() {
        return proofSystem;
    }

    public void setProofSystem(String proofSystem) {
        this.proofSystem = proofSystem;
    }

    public String getProofVersion() {
        return proofVersion;
    }

    public void setProofVersion(String proofVersion) {
        this.proofVersion = proofVersion;
    }

    public String getStatementDescription() {
        return statementDescription;
    }

    public void setStatementDescription(String statementDescription) {
        this.statementDescription = statementDescription;
    }

    public String getPublicInputs() {
        return publicInputs;
    }

    public void setPublicInputs(String publicInputs) {
        this.publicInputs = publicInputs;
    }

    public String getVerificationKeyReference() {
        return verificationKeyReference;
    }

    public void setVerificationKeyReference(String verificationKeyReference) {
        this.verificationKeyReference = verificationKeyReference;
    }

    public String getProofReference() {
        return proofReference;
    }

    public void setProofReference(String proofReference) {
        this.proofReference = proofReference;
    }

    public String getProofHash() {
        return proofHash;
    }

    public void setProofHash(String proofHash) {
        this.proofHash = proofHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
