package org.demo.health.monitoring.keystore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public class CreateKeyStore {
    private KeyStore keyStore;
    private String keyStoreName;
    private String keyStoreType;
    private String keyStorePassword;

    CreateKeyStore(String keyStoreType, String keyStorePassword, String keyStoreName) {
        this.keyStoreName = keyStoreName;
        this.keyStoreType = keyStoreType;
        this.keyStorePassword = keyStorePassword;
    }

    void createEmptyKeyStore() {
        if (keyStoreType == null || keyStoreType.isEmpty()) {
            keyStoreType = KeyStore.getDefaultType();
        }

        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            //load
            char[] pwdArray = keyStorePassword.toCharArray();
            keyStore.load(null, pwdArray);
            // Save the keyStore
            FileOutputStream fos = new FileOutputStream(keyStoreName);
            keyStore.store(fos, pwdArray);
            fos.close();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    void loadKeyStore() {
        char[] pwdArray = keyStorePassword.toCharArray();
        try {
            FileInputStream fis = new FileInputStream(keyStoreName);
            keyStore.load(fis, pwdArray);
            fis.close();
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    void setEntry(String alias, KeyStore.SecretKeyEntry secretKeyEntry, KeyStore.ProtectionParameter protectionParameter) {
        try {
            keyStore.setEntry(alias, secretKeyEntry, protectionParameter);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    KeyStore.Entry getEntry(String alias) {
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keyStorePassword.toCharArray());
        try {
            return keyStore.getEntry(alias, protParam);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    void setKeyEntry(String alias, PrivateKey privateKey, String keyPassword, Certificate[] certificateChain) {
        try {
            keyStore.setKeyEntry(alias, privateKey, keyPassword.toCharArray(), certificateChain);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    void setCertificateEntry(String alias, Certificate certificate) {
        try {
            keyStore.setCertificateEntry(alias, certificate);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    Certificate getCertificate(String alias) {
        try {
            return keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    void deleteEntry(String alias) {
        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    void deleteKeyStore() {
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                keyStore.deleteEntry(alias);
            }
            keyStore = null;

            Path keyStoreFile = Paths.get(keyStoreName);
            Files.delete(keyStoreFile);
        } catch (KeyStoreException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    KeyStore getKeyStore() {
        return this.keyStore;
    }
}