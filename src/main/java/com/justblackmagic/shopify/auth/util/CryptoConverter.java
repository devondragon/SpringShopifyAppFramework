package com.justblackmagic.shopify.auth.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA AttributeConverter for encrypting a String value using AES-GCM authenticated encryption.
 *
 * <p>This converter uses AES-GCM (Galois/Counter Mode) which provides both confidentiality
 * and authenticity. The encrypted output format is:
 * <pre>
 * MAGIC_HEADER (4 bytes "GCM1") + IV (12 bytes) + CIPHERTEXT + AUTH_TAG (16 bytes)
 * </pre>
 *
 * <p>For backward compatibility, this converter can also decrypt legacy data that was
 * encrypted using the old AES-ECB algorithm. Legacy data is detected by the absence of
 * the magic header prefix.
 *
 * <p>Thread Safety: This class is thread-safe. The SecureRandom instance is thread-safe
 * in modern Java implementations (Java 8+).
 *
 * @author justblackmagic
 */
@Slf4j
@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String AES = "AES";
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final String LEGACY_ECB_ALGORITHM = "AES/ECB/PKCS5Padding";

    /** GCM IV length in bytes (96 bits recommended by NIST) */
    private static final int GCM_IV_LENGTH = 12;

    /** GCM authentication tag length in bits */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Magic header to identify GCM-encrypted data.
     * Using 4 bytes makes false positives extremely unlikely (1 in 2^32).
     */
    private static final byte[] GCM_MAGIC_HEADER = {(byte) 0x47, (byte) 0x43, (byte) 0x4D, (byte) 0x31}; // "GCM1"

    /** Length of magic header */
    private static final int MAGIC_HEADER_LENGTH = GCM_MAGIC_HEADER.length;

    /** Minimum length for valid GCM data: magic header + IV + auth tag (empty plaintext) */
    private static final int MIN_GCM_DATA_LENGTH = MAGIC_HEADER_LENGTH + GCM_IV_LENGTH + (GCM_TAG_LENGTH / 8);

    /** Maximum allowed encrypted data size (1 MB) to prevent memory exhaustion attacks */
    private static final int MAX_ENCRYPTED_DATA_SIZE = 1024 * 1024;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * The key used to encrypt and decrypt the data. Base64 encoded AES key with 256 bits of data.
     */
    @Value("${shopify.auth.tokenEncryptionKey}")
    private String encryptionKeyString;

    /**
     * Encrypts the attribute value using AES-GCM.
     *
     * @param plaintext the plaintext to encrypt
     * @return Base64-encoded ciphertext with magic header and IV prepended
     */
    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            Key key = new SecretKeySpec(Base64.getDecoder().decode(encryptionKeyString), AES);

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher with GCM parameters
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine: MAGIC_HEADER + IV + CIPHERTEXT (includes auth tag)
            ByteBuffer buffer = ByteBuffer.allocate(MAGIC_HEADER_LENGTH + GCM_IV_LENGTH + ciphertext.length);
            buffer.put(GCM_MAGIC_HEADER);
            buffer.put(iv);
            buffer.put(ciphertext);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            log.error("CryptoConverter.convertToDatabaseColumn: Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts the attribute value. Supports both AES-GCM (new) and AES-ECB (legacy) formats.
     *
     * @param dbData the Base64-encoded encrypted data
     * @return the decrypted plaintext
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            byte[] encryptedData = Base64.getDecoder().decode(dbData);

            // Validate size to prevent memory exhaustion attacks
            if (encryptedData.length > MAX_ENCRYPTED_DATA_SIZE) {
                throw new IllegalArgumentException("Encrypted data exceeds maximum allowed size");
            }

            // Check if this is GCM-encrypted data by looking for magic header
            if (hasGcmMagicHeader(encryptedData)) {
                return decryptGcm(encryptedData);
            } else {
                // Legacy ECB-encrypted data
                return decryptLegacyEcb(encryptedData);
            }
        } catch (IllegalArgumentException e) {
            log.error("CryptoConverter.convertToEntityAttribute: Invalid data", e);
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("CryptoConverter.convertToEntityAttribute: Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Checks if the encrypted data has the GCM magic header.
     */
    private boolean hasGcmMagicHeader(byte[] data) {
        if (data.length < MAGIC_HEADER_LENGTH) {
            return false;
        }
        for (int i = 0; i < MAGIC_HEADER_LENGTH; i++) {
            if (data[i] != GCM_MAGIC_HEADER[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Decrypts data encrypted with AES-GCM.
     *
     * @param encryptedData the encrypted data including magic header, IV, ciphertext, and auth tag
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the data is too short to be valid GCM data
     */
    private String decryptGcm(byte[] encryptedData) throws Exception {
        // Validate minimum length: magic header + IV + auth tag
        if (encryptedData.length < MIN_GCM_DATA_LENGTH) {
            throw new IllegalArgumentException("Invalid GCM encrypted data: too short");
        }

        Key key = new SecretKeySpec(Base64.getDecoder().decode(encryptionKeyString), AES);

        // Extract IV and ciphertext (skip magic header)
        ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
        buffer.position(MAGIC_HEADER_LENGTH); // Skip magic header

        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);

        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        // Decrypt
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    /**
     * Decrypts legacy data encrypted with AES-ECB.
     *
     * @deprecated This method exists only for backward compatibility with existing encrypted data.
     *             All new encryption uses AES-GCM.
     */
    @Deprecated
    private String decryptLegacyEcb(byte[] encryptedData) throws Exception {
        log.debug("Decrypting legacy ECB-encrypted data. Consider re-encrypting with GCM.");

        Key key = new SecretKeySpec(Base64.getDecoder().decode(encryptionKeyString), AES);
        Cipher cipher = Cipher.getInstance(LEGACY_ECB_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8);
    }
}
