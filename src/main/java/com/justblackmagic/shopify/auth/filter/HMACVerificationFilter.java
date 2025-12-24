package com.justblackmagic.shopify.auth.filter;

import java.io.IOException;
import com.justblackmagic.shopify.auth.util.ShopifyHMACValidator;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet Filter that verifies the authenticity of requests from Shopify using HMAC signatures.
 *
 * This filter enforces HMAC validation for Shopify OAuth callbacks:
 * - OAuth callback URLs (containing 'oauth2/code') REQUIRE a valid HMAC
 * - Other URLs with an HMAC parameter must have a valid HMAC (blocks tampering)
 * - URLs without an HMAC parameter are allowed through (handled by other security)
 *
 * @author justblackmagic
 * @see <a href="https://shopify.dev/docs/apps/build/authentication-authorization">Shopify Auth Docs</a>
 */
@Slf4j
@Data
public class HMACVerificationFilter implements Filter {

    private static final String OAUTH_CALLBACK_PATH = "oauth2/code";

    // Normally we'd Autowire this, but since we are using the FilterRegistrationConfig class, we have to manually inject it from there
    private ShopifyHMACValidator shopifyHMACValidator;


    /**
     * Filters requests to validate Shopify HMAC signatures.
     *
     * @param request the servlet request
     * @param response the servlet response
     * @param chain the filter chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("HMACVerificationFilter.doFilter() - called...");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String hmacParam = httpRequest.getParameter("hmac");
        String requestUri = httpRequest.getRequestURI();
        boolean isOAuthCallback = requestUri != null && requestUri.contains(OAUTH_CALLBACK_PATH);

        // OAuth callbacks MUST have an HMAC parameter
        if (isOAuthCallback && (hmacParam == null || hmacParam.isEmpty())) {
            log.error("HMACVerificationFilter: OAuth callback missing required HMAC parameter");
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing HMAC signature");
            return;
        }

        // If no HMAC parameter present (and not OAuth callback), allow through
        // Other security mechanisms (session, Spring Security) will handle authorization
        if (hmacParam == null || hmacParam.isEmpty()) {
            log.debug("HMACVerificationFilter: No HMAC parameter, allowing request");
            request.setAttribute("shopifyHMACValid", false);
            chain.doFilter(request, response);
            return;
        }

        // Validate the HMAC signature
        if (shopifyHMACValidator.validateHMAC(httpRequest)) {
            log.debug("HMACVerificationFilter.doFilter() - HMAC is valid");
            request.setAttribute("shopifyHMACValid", true);
            chain.doFilter(request, response);
        } else {
            // HMAC present but invalid - potential tampering, block the request
            log.error("HMACVerificationFilter.doFilter() - HMAC validation failed for URI: {}", requestUri);
            request.setAttribute("shopifyHMACValid", false);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid HMAC signature");
        }
    }

}
