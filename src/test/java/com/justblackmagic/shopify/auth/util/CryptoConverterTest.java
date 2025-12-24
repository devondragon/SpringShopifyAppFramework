package com.justblackmagic.shopify.auth.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for CryptoConverter to verify AES-GCM encryption and backward compatibility with AES-ECB.
 */
class CryptoConverterTest {

    private CryptoConverter converter;

    // Test encryption key (256-bit AES key, Base64 encoded)
    private static final String TEST_KEY = "dGVzdGtleXRlc3RrZXl0ZXN0a2V5dGVzdGtleXRlc3Q=";

    @BeforeEach
    void setUp() throws Exception {
        converter = new CryptoConverter();
        // Use reflection to set the encryption key since @Value won't work in unit tests
        Field keyField = CryptoConverter.class.getDeclaredField("encryptionKeyString");
        keyField.setAccessible(true);
        keyField.set(converter, TEST_KEY);
    }

    @Test
    void testEncryptAndDecrypt() {
        String plaintext = "shpat_test_access_token_12345";

        String encrypted = converter.convertToDatabaseColumn(plaintext);
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);

        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testEncryptionProducesDifferentCiphertextEachTime() {
        String plaintext = "same_token_value";

        String encrypted1 = converter.convertToDatabaseColumn(plaintext);
        String encrypted2 = converter.convertToDatabaseColumn(plaintext);

        // GCM uses random IV, so same plaintext should produce different ciphertext
        assertNotEquals(encrypted1, encrypted2);

        // But both should decrypt to the same value
        assertEquals(plaintext, converter.convertToEntityAttribute(encrypted1));
        assertEquals(plaintext, converter.convertToEntityAttribute(encrypted2));
    }

    @Test
    void testNullHandling() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testEmptyString() {
        String encrypted = converter.convertToDatabaseColumn("");
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals("", decrypted);
    }

    @Test
    void testLongString() {
        String plaintext = "a".repeat(10000);
        String encrypted = converter.convertToDatabaseColumn(plaintext);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testSpecialCharacters() {
        String plaintext = "token_with_special_chars_!@#$%^&*()_+-={}[]|\\:\";<>?,./~`";
        String encrypted = converter.convertToDatabaseColumn(plaintext);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testUnicodeCharacters() {
        String plaintext = "token_with_unicode_日本語_中文_한국어_🔐";
        String encrypted = converter.convertToDatabaseColumn(plaintext);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testBackwardCompatibilityWithLegacyEcbEncryption() throws Exception {
        // Simulate legacy ECB-encrypted data
        String plaintext = "legacy_access_token";
        String legacyEncrypted = encryptWithLegacyEcb(plaintext);

        // The converter should be able to decrypt legacy data
        String decrypted = converter.convertToEntityAttribute(legacyEncrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testGcmEncryptedDataStartsWithMagicHeader() {
        String plaintext = "test_token";
        String encrypted = converter.convertToDatabaseColumn(plaintext);

        byte[] decoded = Base64.getDecoder().decode(encrypted);
        // First 4 bytes should be magic header "GCM1"
        assertEquals((byte) 0x47, decoded[0]); // 'G'
        assertEquals((byte) 0x43, decoded[1]); // 'C'
        assertEquals((byte) 0x4D, decoded[2]); // 'M'
        assertEquals((byte) 0x31, decoded[3]); // '1'
    }

    /**
     * Helper method to encrypt data using the legacy ECB algorithm.
     * This simulates data that was encrypted before the GCM upgrade.
     */
    private String encryptWithLegacyEcb(String plaintext) throws Exception {
        Key key = new SecretKeySpec(Base64.getDecoder().decode(TEST_KEY), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
}
