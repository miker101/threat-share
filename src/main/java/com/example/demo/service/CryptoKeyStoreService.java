package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.EnumSet;
import java.util.Set;

import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.springframework.stereotype.Service;

import com.example.demo.config.CryptoKeyStoreProperties;

import java.security.Key;

@Service
public class CryptoKeyStoreService {

    private final CryptoKeyStoreProperties properties;
    private final KeyStore keyStore;

    public CryptoKeyStoreService(CryptoKeyStoreProperties properties)
            throws GeneralSecurityException, IOException {

        this.properties = properties;
        this.keyStore = loadOrCreateKeyStore();
    }

    private KeyStore loadOrCreateKeyStore()
            throws GeneralSecurityException, IOException {

        String configuredPath = properties.getPath();
        String password = properties.getPassword();
        String type = properties.getType();

        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalStateException(
                    "THREATSHARE_KEYSTORE_PATH is not configured.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "THREATSHARE_KEYSTORE_PASSWORD is not configured.");
        }

        Path path = Paths.get(configuredPath).toAbsolutePath().normalize();

        Path parent = path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        KeyStore store = KeyStore.getInstance(type);

        if (Files.exists(path)) {

            try (InputStream input = Files.newInputStream(path)) {
                store.load(input, password.toCharArray());
            }

        } else {

            store.load(null, password.toCharArray());

            try (OutputStream output = Files.newOutputStream(path)) {
                store.store(output, password.toCharArray());
            }

            restrictFilePermissions(path);
        }

        return store;
    }

    private void restrictFilePermissions(Path path) {

        try {
            Set<PosixFilePermission> permissions = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
            );

            Files.setPosixFilePermissions(path, permissions);

        } catch (UnsupportedOperationException | IOException e) {
            // Windows does not support POSIX permissions.
            // Configure Windows NTFS permissions separately.
        }
    }

    public synchronized void storePrivateKey(
            String alias,
            PrivateKey privateKey,
            Certificate certificate
    ) throws GeneralSecurityException, IOException {

        if (alias == null || alias.isBlank()) {
            throw new IllegalArgumentException(
                    "Keystore alias cannot be empty.");
        }

        if (privateKey == null || certificate == null) {
            throw new IllegalArgumentException(
                    "Private key and certificate are required.");
        }

        // Only RSA private keys are accepted by this method.
        if (!"RSA".equalsIgnoreCase(privateKey.getAlgorithm())) {
            throw new IllegalArgumentException(
                    "Only RSA private keys are supported.");
        }

        // Prevent accidental replacement of existing keys.
        if (keyStore.containsAlias(alias)) {
            throw new IllegalStateException(
                    "A key already exists with alias: " + alias);
        }

        // Store the private key with its corresponding certificate.
        keyStore.setKeyEntry(
                alias,
                privateKey,
                properties.getPassword().toCharArray(),
                new Certificate[]{certificate}
        );

        try {

            // Persist the updated keystore.
            saveKeyStore();

        } catch (GeneralSecurityException | IOException e) {

            // Roll back the in-memory entry if persistence fails.
            keyStore.deleteEntry(alias);

            throw e;
        }
    }
    public synchronized void removePrivateKey(String alias)
            throws GeneralSecurityException, IOException {

        if (alias == null || alias.isBlank()) {
            throw new IllegalArgumentException(
                    "Keystore alias cannot be empty.");
        }

        if (!keyStore.containsAlias(alias)) {
            return;
        }

        keyStore.deleteEntry(alias);

        saveKeyStore();
    }

    public synchronized void saveKeyStore()
            throws GeneralSecurityException, IOException {

        Path path = Paths.get(properties.getPath())
                .toAbsolutePath()
                .normalize();

        try (OutputStream output = Files.newOutputStream(path)) {
            keyStore.store(output, properties.getPassword().toCharArray());
        }
    }

public synchronized PrivateKey getPrivateKey(String alias)
        throws GeneralSecurityException {

    if (alias == null || alias.isBlank()) {
        throw new IllegalArgumentException(
                "Keystore alias cannot be empty.");
    }

    if (!keyStore.containsAlias(alias)) {
        throw new IllegalStateException(
                "Signing key not found in keystore: " + alias);
    }

    Key key = keyStore.getKey(
            alias,
            properties.getPassword().toCharArray()
    );

    if (!(key instanceof PrivateKey privateKey)) {
        throw new GeneralSecurityException(
                "The requested alias does not contain a private key.");
    }

    return privateKey;
}

}