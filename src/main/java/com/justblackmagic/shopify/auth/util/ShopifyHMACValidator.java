package com.justblackmagic.shopify.auth.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
 * Currently is only working on some requests, not all. More debugging needs to be done.
 *
 * @author justblackmagic
 */

@Slf4j
@Service
public class ShopifyHMACValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";
    @Value("${shopify.auth.client-secret}")
    private String secret;

    /**
     * Validates the HMAC signature on the incoming request.
     *
     * For GET and POST requests, checks to see if there is an HMAC to validate, and if so, validates it against the correct request data.
     *
     * @param request HttpServletRequest
     * @return true if valid, false if not
     */
    public boolean validateHMAC(HttpServletRequest request) {
        log.debug("Validating HMAC for request: {}", request.toString());
        boolean valid = false;

        if (request.getMethod().equals("GET")) {
            String hmac = request.getParameter("hmac");
            // If we have an hmac parameter, we need to validate it.
            if (StringUtils.hasText(hmac)) {
                String queryString = request.getQueryString();
                // We have to remove the hmac parameter from the query string, since it's not part of the data we're validating.
                // It may be better to pull the query params into a collection, remove the hmac param, and then re-build the query string, but this
                // seems to be working
                String data = queryString.replaceAll("hmac=" + hmac + "&?", "");



                if (data.endsWith("&") && data.length() > 1) {
                    data = data.substring(0, data.length() - 1);
                }
                // https://github.com/devondragon/SpringShopifyAppFramework/issues/9
                data = URLDecoder.decode(data, StandardCharsets.UTF_8);

                log.debug("HMAC: {}", hmac);
                log.debug("Data: {}", data);
                log.trace("Secret: {}", secret);
                try {
                    valid = verifyHmac(data, hmac, secret);
                } catch (IllegalStateException e) {
                    log.error("validateHMAC: IllegalStateException!", e);
                }
            } else {
                log.debug("validateHMAC called with a GET method and no hmac query param.");
            }
        } else {
            // Currently not validatng POST requests as accessing the body is generally destructive.
            log.info("validateHMAC: called with request that is not GET!");
        }

        log.debug("validateHMAC returning: {}", valid);
        return valid;
    }

    public boolean validatePostHMAC(HttpServletRequest request, String requestBodyString) {
        log.debug("validatePostHMAC: Validating HMAC for request: {}", request.toString());
        boolean valid = false;
        String hmac = request.getHeader("X-Shopify-Hmac-Sha256");
        // If we have an hmac parameter, we need to validate it.
        if (StringUtils.hasText(hmac)) {
            try {
                valid = verifyHmac(requestBodyString, hmac, secret);
            } catch (IllegalStateException e) {
                log.error("Error while validating HMAC", e);
            }
        } else {
            log.debug("validatePostHMAC: called with no hmac header.");
        }

        log.debug("validatePostHMAC: returning: {}", valid);
        return valid;
    }


    /**
     * Verifies the HMAC signature against the provided data and secret.
     *
     * @param data
     * @param hmac
     * @param secret
     * @return
     * @throws IllegalStateException
     * @throws UnsupportedEncodingException
     */
    public boolean verifyHmac(String data, String hmac, String secret) {
        try {
            Mac hmacSHA256 = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            hmacSHA256.init(key);
            byte[] rawHmac = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String calculated = Base64.getEncoder().encodeToString(rawHmac);
            return hmac.equals(calculated);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            log.error("Error verifying hmac", ex);
            throw new IllegalArgumentException(ex);
        }
    }
}
