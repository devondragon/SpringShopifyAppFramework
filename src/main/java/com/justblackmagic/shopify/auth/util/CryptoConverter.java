package com.justblackmagic.shopify.auth.util;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;

/**
 * JPA AttributeConverter for encrypting a String value.
 * 
 * @author justblackmagic
 * 
 */
@Slf4j
@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

	private static final String AES = "AES";

	private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

	/**
	 * The key used to encrypt and decrypt the data. Base64 encoded AES key with 256 bits of data.
	 */
	@Value("${shopify.auth.tokenEncryptionKey}")
	private String encryptionKeyString;


	/**
	 * @param ccNumber
	 * @return String
	 */
	@Override
	public String convertToDatabaseColumn(String ccNumber) {
		// do some encryption
		Key key = new SecretKeySpec(Base64.getDecoder().decode(encryptionKeyString), AES);
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			return Base64.getEncoder().encodeToString(c.doFinal(ccNumber.getBytes()));
		} catch (Exception e) {
			log.error("CryptoConverter.convertToDatabaseColumn: Exception!", e);
			throw new RuntimeException(e);
		}
	}


	/**
	 * @param dbData
	 * @return String
	 */
	@Override
	public String convertToEntityAttribute(String dbData) {
		// do some decryption
		Key key = new SecretKeySpec(Base64.getDecoder().decode(encryptionKeyString), AES);
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key);
			return new String(c.doFinal(Base64.getDecoder().decode(dbData)));
		} catch (Exception e) {
			log.error("CryptoConverter.convertToEntityAttribute: Exception!", e);
			throw new RuntimeException(e);
		}
	}
}
