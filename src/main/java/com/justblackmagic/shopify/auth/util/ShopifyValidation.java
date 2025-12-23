package com.justblackmagic.shopify.auth.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating and sanitizing Shopify-related inputs.
 * Provides validation for shop names to prevent injection attacks.
 *
 * @author justblackmagic
 */
public class ShopifyValidation {

	/**
	 * Pattern for valid Shopify shop names.
	 * Format: {store-name}.myshopify.com
	 * - Must start with alphanumeric character
	 * - Can contain lowercase letters, numbers, and hyphens
	 * - Must end with .myshopify.com
	 */
	private static final Pattern SHOP_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9\\-]*\\.myshopify\\.com$");

	private ShopifyValidation() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Validates whether a shop name matches the required Shopify format.
	 *
	 * @param shopName the shop name to validate
	 * @return true if the shop name is valid, false otherwise
	 */
	public static boolean isValidShopName(String shopName) {
		return shopName != null && SHOP_NAME_PATTERN.matcher(shopName).matches();
	}

	/**
	 * Sanitizes a shop name by validating it against the required format.
	 * Throws an exception if the shop name is invalid to prevent injection attacks.
	 *
	 * @param shopName the shop name to sanitize
	 * @return the validated shop name
	 * @throws IllegalArgumentException if the shop name doesn't match the required format
	 */
	public static String sanitizeShopName(String shopName) {
		if (!isValidShopName(shopName)) {
			throw new IllegalArgumentException("Invalid shop name format: " + shopName);
		}
		return shopName;
	}

}
