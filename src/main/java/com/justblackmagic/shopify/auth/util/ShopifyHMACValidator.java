package com.justblackmagic.shopify.auth.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * A Service to validate and verify the Shopify HMAC signature on incoming requests.
 *
 * Shopify uses different HMAC encodings for different contexts:
 * - OAuth/Install callbacks (GET): HMAC in query parameter, encoded as HEX
 * - Webhooks (POST): HMAC in X-Shopify-Hmac-SHA256 header, encoded as Base64
 *
 * @author justblackmagic
 * @see <a href="https://shopify.dev/docs/apps/build/authentication-authorization/access-tokens/authorization-code-grant">Shopify OAuth Docs</a>
 * @see <a href="https://shopify.dev/docs/apps/build/webhooks/subscribe/https">Shopify Webhook Docs</a>
 */

@Slf4j
@Service
public class ShopifyHMACValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";
    @Value("${shopify.auth.client-secret}")
    private String secret;

    /**
     * Validates the HMAC signature on the incoming GET request (OAuth callbacks).
     *
     * Shopify OAuth callbacks include an HMAC parameter in the query string, encoded as HEX.
     * This method extracts the HMAC, reconstructs the data string, and verifies the signature.
     *
     * @param request HttpServletRequest
     * @return true if valid, false if not (or if no HMAC present)
     */
    public boolean validateHMAC(HttpServletRequest request) {
        log.debug("Validating HMAC for request: {}", request.getRequestURI());

        if (!"GET".equals(request.getMethod())) {
            log.debug("validateHMAC: called with non-GET method. Use validatePostHMAC for POST requests.");
            return false;
        }

        String hmac = request.getParameter("hmac");
        if (!StringUtils.hasText(hmac)) {
            log.debug("validateHMAC: No hmac query parameter present.");
            return false;
        }

        String queryString = request.getQueryString();
        // Remove the hmac parameter from the query string since it's not part of the data we're validating
        String data = queryString.replaceAll("hmac=" + hmac + "&?", "");

        // Clean up trailing ampersand if present
        if (data.endsWith("&") && data.length() > 1) {
            data = data.substring(0, data.length() - 1);
        }

        // URL decode the data (https://github.com/devondragon/SpringShopifyAppFramework/issues/9)
        data = URLDecoder.decode(data, StandardCharsets.UTF_8);

        log.debug("HMAC: {}", hmac);
        log.debug("Data: {}", data);
        // Never log secrets, even at trace level
        log.trace("Validating HMAC with configured secret");

        try {
            // OAuth callbacks use HEX encoding (not Base64)
            boolean valid = verifyHmacHex(data, hmac, secret);
            log.debug("validateHMAC returning: {}", valid);
            return valid;
        } catch (IllegalArgumentException e) {
            log.error("validateHMAC: Error verifying HMAC", e);
            return false;
        }
    }

    /**
     * Validates the HMAC signature on incoming POST requests (webhooks).
     *
     * Shopify webhooks include an HMAC in the X-Shopify-Hmac-SHA256 header, encoded as Base64.
     * The HMAC is calculated from the raw request body.
     *
     * @param request HttpServletRequest
     * @param requestBodyString the raw request body as a string
     * @return true if valid, false if not (or if no HMAC header present)
     */
    public boolean validatePostHMAC(HttpServletRequest request, String requestBodyString) {
        log.debug("validatePostHMAC: Validating HMAC for request: {}", request.getRequestURI());

        String hmac = request.getHeader("X-Shopify-Hmac-Sha256");
        if (!StringUtils.hasText(hmac)) {
            log.debug("validatePostHMAC: No X-Shopify-Hmac-Sha256 header present.");
            return false;
        }

        // Never log secrets, even at trace level
        log.trace("Validating webhook HMAC with configured secret");

        try {
            // Webhooks use Base64 encoding
            boolean valid = verifyHmacBase64(requestBodyString, hmac, secret);
            log.debug("validatePostHMAC returning: {}", valid);
            return valid;
        } catch (IllegalArgumentException e) {
            log.error("validatePostHMAC: Error verifying HMAC", e);
            return false;
        }
    }


    /**
     * Verifies the HMAC signature using HEX encoding.
     * Used for OAuth/install callbacks where Shopify sends HMAC in query parameter.
     *
     * @param data the data string to verify
     * @param hmac the expected HMAC value (hex-encoded)
     * @param secret the client secret
     * @return true if the calculated HMAC matches the provided HMAC
     * @throws IllegalArgumentException if there's an error computing the HMAC
     */
    public boolean verifyHmacHex(String data, String hmac, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(key);
            byte[] calculatedHmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Decode the expected hex HMAC to bytes for timing-safe comparison
            byte[] expectedHmacBytes = HexFormat.of().parseHex(hmac);

            // Use timing-safe comparison on raw bytes to prevent timing attacks
            return MessageDigest.isEqual(expectedHmacBytes, calculatedHmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException ex) {
            log.error("Error verifying HMAC (hex)", ex);
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Verifies the HMAC signature using Base64 encoding.
     * Used for webhook callbacks where Shopify sends HMAC in X-Shopify-Hmac-SHA256 header.
     *
     * @param data the data string to verify (raw request body)
     * @param hmac the expected HMAC value (base64-encoded)
     * @param secret the client secret
     * @return true if the calculated HMAC matches the provided HMAC
     * @throws IllegalArgumentException if there's an error computing the HMAC
     */
    public boolean verifyHmacBase64(String data, String hmac, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(key);
            byte[] calculatedHmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Decode the expected Base64 HMAC to bytes for timing-safe comparison
            byte[] expectedHmacBytes = Base64.getDecoder().decode(hmac);

            // Use timing-safe comparison on raw bytes to prevent timing attacks
            return MessageDigest.isEqual(expectedHmacBytes, calculatedHmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException ex) {
            log.error("Error verifying HMAC (base64)", ex);
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * @deprecated Use {@link #verifyHmacHex(String, String, String)} for OAuth callbacks
     *             or {@link #verifyHmacBase64(String, String, String)} for webhooks.
     *             This method uses Base64 encoding which is only correct for webhooks.
     */
    @Deprecated
    public boolean verifyHmac(String data, String hmac, String secret) {
        return verifyHmacBase64(data, hmac, secret);
    }
}
