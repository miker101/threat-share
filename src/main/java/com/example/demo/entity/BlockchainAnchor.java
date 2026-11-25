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
@Table(name = "blockchain_anchors")
public class BlockchainAnchor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "anchor_type", nullable = false, length = 80)
    private String anchorType;

    @Column(name = "source_record_type", nullable = false, length = 80)
    private String sourceRecordType;

    @Column(name = "source_record_id", nullable = false)
    private Long sourceRecordId;

    @Column(name = "content_hash", nullable = false, length = 128)
    private String contentHash;

    @Column(name = "hash_algorithm", nullable = false, length = 40)
    private String hashAlgorithm = "SHA-256";

    @Column(name = "blockchain_network", nullable = false, length = 100)
    private String blockchainNetwork;

    @Column(name = "blockchain_transaction_hash", length = 255)
    private String blockchainTransactionHash;

    @Column(name = "blockchain_block_number")
    private Long blockchainBlockNumber;

    @Column(name = "blockchain_contract_address", length = 255)
    private String blockchainContractAddress;

    @Column(name = "anchor_status", nullable = false, length = 30)
    private String anchorStatus = "PENDING";

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "PENDING";

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getAnchorType() {
        return anchorType;
    }

    public void setAnchorType(String anchorType) {
        this.anchorType = anchorType;
    }

    public String getSourceRecordType() {
        return sourceRecordType;
    }

    public void setSourceRecordType(String sourceRecordType) {
        this.sourceRecordType = sourceRecordType;
    }

    public Long getSourceRecordId() {
        return sourceRecordId;
    }

    public void setSourceRecordId(Long sourceRecordId) {
        this.sourceRecordId = sourceRecordId;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getBlockchainNetwork() {
        return blockchainNetwork;
    }

    public void setBlockchainNetwork(String blockchainNetwork) {
        this.blockchainNetwork = blockchainNetwork;
    }

    public String getBlockchainTransactionHash() {
        return blockchainTransactionHash;
    }

    public void setBlockchainTransactionHash(String blockchainTransactionHash) {
        this.blockchainTransactionHash = blockchainTransactionHash;
    }

    public Long getBlockchainBlockNumber() {
        return blockchainBlockNumber;
    }

    public void setBlockchainBlockNumber(Long blockchainBlockNumber) {
        this.blockchainBlockNumber = blockchainBlockNumber;
    }

    public String getBlockchainContractAddress() {
        return blockchainContractAddress;
    }

    public void setBlockchainContractAddress(String blockchainContractAddress) {
        this.blockchainContractAddress = blockchainContractAddress;
    }

    public String getAnchorStatus() {
        return anchorStatus;
    }

    public void setAnchorStatus(String anchorStatus) {
        this.anchorStatus = anchorStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getLastVerifiedAt() {
        return lastVerifiedAt;
    }

    public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) {
        this.lastVerifiedAt = lastVerifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}