package com.justblackmagic.shopify.auth.filter;

import java.io.IOException;
import java.util.Base64;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter that grabs the Shopify shop name from the request and stores it in the session for later use.
 * Supports both direct 'shop' parameter and Base64-encoded 'host' parameter used by embedded apps.
 *
 * @author justblackmagic
 */
@Slf4j
public class ShopifyShopNameFilter implements Filter {

    /**
     * This method looks for the shop name in the request and stores it in the session.
     * It checks both the 'shop' parameter (direct) and 'host' parameter (Base64 encoded for embedded apps).
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String shopName = request.getParameter(AuthConstants.SHOP_ATTRIBUE_NAME);
        log.debug("ShopName from 'shop' parameter: {}", shopName);

        // If shop name not found directly, try to extract from Base64-encoded 'host' parameter
        // Shopify sends this for embedded apps
        if (shopName == null || shopName.isEmpty()) {
            String host = request.getParameter("host");
            if (host != null && !host.isEmpty()) {
                shopName = extractShopNameFromHost(host);
                log.debug("ShopName extracted from 'host' parameter: {}", shopName);
            }
        }

        if (shopName != null && !shopName.isEmpty()) {
            httpRequest.getSession().setAttribute(AuthConstants.SHOP_ATTRIBUE_NAME, shopName);
            request.setAttribute("shopName", shopName);
            log.debug("ShopName '{}' added to session.", shopName);
        }
        chain.doFilter(request, response);
    }

    /**
     * Extracts the shop name from a Base64-encoded host parameter.
     * The host parameter from Shopify is Base64 encoded and contains the shop's admin URL.
     *
     * @param host the Base64-encoded host string
     * @return the extracted shop name (e.g., "mystore.myshopify.com") or null if extraction fails
     */
    private String extractShopNameFromHost(String host) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(host);
            String decodedHost = new String(decodedBytes);
            log.debug("Decoded host: {}", decodedHost);

            // The decoded host typically looks like:
            // "admin.shopify.com/store/mystore" or "mystore.myshopify.com/admin"
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
