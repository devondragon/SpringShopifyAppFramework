package com.justblackmagic.shopify.auth.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for extracting shop information from Shopify host parameters.
 * The host parameter is Base64 encoded and contains the shop's admin URL.
 *
 * @author justblackmagic
 */
@Slf4j
public final class ShopifyHostUtils {

    private ShopifyHostUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts the shop name from a Base64-encoded host parameter.
     * The host parameter from Shopify is Base64 encoded and contains the shop's admin URL.
     *
     * @param host the Base64-encoded host string
     * @return the extracted shop name (e.g., "mystore.myshopify.com") or null if extraction fails
     */
    public static String extractShopNameFromHost(String host) {
        if (host == null || host.isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(host);
            String decodedHost = new String(decodedBytes, StandardCharsets.UTF_8);
            log.debug("Decoded host: {}", decodedHost);

            String shopName = null;

            if (decodedHost.contains("admin.shopify.com/store/")) {
                // Format: admin.shopify.com/store/mystore
                String[] parts = decodedHost.split("/store/");
                if (parts.length > 1) {
                    String storePart = parts[1].split("/")[0]; // Get just the store name
                    shopName = storePart + ".myshopify.com";
                }
            } else if (decodedHost.contains(".myshopify.com")) {
                // Format: mystore.myshopify.com/admin or similar
                // Extract the myshopify.com domain
                int startIndex = decodedHost.indexOf("://");
                if (startIndex != -1) {
                    decodedHost = decodedHost.substring(startIndex + 3);
                }
                // Remove any path
                if (decodedHost.contains("/")) {
                    shopName = decodedHost.substring(0, decodedHost.indexOf("/"));
                } else {
                    shopName = decodedHost;
                }
            }

            log.debug("Extracted shopName from host: {}", shopName);
            return shopName;
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode host parameter: {}", e.getMessage());
            return null;
        }
    }
}
