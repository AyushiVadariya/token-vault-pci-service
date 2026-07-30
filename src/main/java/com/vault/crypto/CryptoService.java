package com.vault.crypto;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int KEY_LENGTH_BITS = 256;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public CryptoService() {
        this.secureRandom = new SecureRandom();
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_LENGTH_BITS, this.secureRandom);
            this.secretKey = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize AES key generator", e);
        }
    }

    public static class EncryptionResult {
        private final String encryptedData;
        private final String iv;

        public EncryptionResult(String encryptedData, String iv) {
            this.encryptedData = encryptedData;
            this.iv = iv;
        }

        public String getEncryptedData() {
            return encryptedData;
        }

        public String getIv() {
            return iv;
        }
    }

    /**
     * Encrypts plainText using AES-256-GCM.
     * Generates a random 12-byte IV.
     */
    public EncryptionResult encrypt(String plainText) throws Exception {
        byte[] ivBytes = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(ivBytes);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        String ivBase64 = Base64.getEncoder().encodeToString(ivBytes);

        return new EncryptionResult(encryptedBase64, ivBase64);
    }

    /**
     * Decrypts encryptedBase64 using the specified ivBase64 and AES-256-GCM.
     */
    public String decrypt(String encryptedBase64, String ivBase64) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] ivBytes = Base64.getDecoder().decode(ivBase64);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }
}
