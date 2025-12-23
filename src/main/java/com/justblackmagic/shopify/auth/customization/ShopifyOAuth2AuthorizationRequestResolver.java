package com.justblackmagic.shopify.auth.customization;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to customize the authorization request.
 *
 * author: justblackmagic
 */
@Slf4j
public class ShopifyOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private ClientRegistrationRepository clientRegistrationRepository;

    private final OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver;

    /**
     * Constructor
     *
     * @param clientRegistrationRepository
     */
    public ShopifyOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.defaultAuthorizationRequestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    /**
     * This method is used to customize the authorization request, primarily by changing the placeholder hostname out for the shop's hostname.
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        log.debug("resolve: Resolving authorization request");
        if (isAuthenticated(request)) {
            log.debug("Request is already authenticated. Returning null.");
            return null;
        }
        // Try to get the shop name from multiple sources
        String shopName = getShopNameFromRequest(request);

        if (shopName == null || shopName.isEmpty()) {
            log.debug("shopName is null or empty - this may be a static resource request or missing shop context");
            // This can happen when the embedded app pulls in JS and other files without the session cookie. So we just move on.
            // Even with permit all, the security filters still run, and in this case try to do an OAuth login. If this ends up not working correctly,
            // then making sure static assets are defined in an ignoring() block in the security config, will fix it.
            return null;

        }

        // We will load up a default authorization request to start
        OAuth2AuthorizationRequest authorizationRequest = this.defaultAuthorizationRequestResolver.resolve(request);
        if (authorizationRequest == null) {
            log.debug("Request is not an authorization request. Returning null.");
            return null;
        }

        String registrationId = "shopify";

        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            throw new IllegalArgumentException("Invalid Client Registration: " + registrationId);
        }

        // This block may be complately unnecessary
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.putAll(authorizationRequest.getAdditionalParameters());
        additionalParameters.put(AuthConstants.SHOP_ATTRIBUE_NAME, shopName);

        // Get the default auth URI and then replace the placeholder hostname with the shop name
        String authUri = authorizationRequest.getAuthorizationUri();
        String updatedAuthUri = authUri.replace("shopname.myshopify.com", shopName);

        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode();

        OAuth2AuthorizationRequest customAuthorizationRequest = builder.clientId(clientRegistration.getClientId()).authorizationUri(updatedAuthUri)
                .redirectUri(authorizationRequest.getRedirectUri()).scopes(clientRegistration.getScopes()).state(authorizationRequest.getState())
                .attributes(authorizationRequest.getAttributes()).additionalParameters(additionalParameters).build();

        log.debug("authRequest.clientId: {}, authUri: {}, redirectUri: {}", customAuthorizationRequest.getClientId(),
                customAuthorizationRequest.getAuthorizationRequestUri(), customAuthorizationRequest.getRedirectUri());

        return customAuthorizationRequest;
    }


    /**
     * @param request
     * @param clientRegistrationId
     * @return OAuth2AuthorizationRequest
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        log.debug("resolve: Resolving authorization request with clientRegId");
        if (isAuthenticated(request)) {
            log.debug("Request is already authenticated. Returning null.");
            return null;
        }
        OAuth2AuthorizationRequest authorizationRequest = this.defaultAuthorizationRequestResolver.resolve(request, clientRegistrationId);

        return authorizationRequest;
    }


    /**
     * @param request
     * @return boolean
     */
    private boolean isAuthenticated(HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken) {
            return true;
        }
        return false;
    }

    /**
     * Attempts to get the shop name from multiple sources in order of preference:
     * 1. Session attribute
     * 2. Request attribute
     * 3. Direct 'shop' request parameter
     * 4. Base64-encoded 'host' request parameter (used by embedded apps)
     *
     * @param request the HTTP request
     * @return the shop name or null if not found
     */
    private String getShopNameFromRequest(HttpServletRequest request) {
        if (request == null) {
            log.error("request is null!");
            return null;
        }

        String shopName = null;

        // 1. Try session attribute
        if (request.getSession() != null && request.getSession().getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME) != null) {
            shopName = request.getSession().getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME).toString();
            log.debug("shopName from session: {}", shopName);
            return shopName;
        }

        // 2. Try request attribute
        if (request.getAttribute("shopName") != null) {
            shopName = request.getAttribute("shopName").toString();
            log.debug("shopName from request attribute: {}", shopName);
            return shopName;
        }

        // 3. Try direct 'shop' parameter
        shopName = request.getParameter(AuthConstants.SHOP_ATTRIBUE_NAME);
        if (shopName != null && !shopName.isEmpty()) {
            log.debug("shopName from 'shop' parameter: {}", shopName);
            // Store in session for future requests
            if (request.getSession() != null) {
                request.getSession().setAttribute(AuthConstants.SHOP_ATTRIBUE_NAME, shopName);
            }
            return shopName;
        }

        // 4. Try Base64-encoded 'host' parameter (embedded apps)
        String host = request.getParameter("host");
        if (host != null && !host.isEmpty()) {
            shopName = extractShopNameFromHost(host);
            if (shopName != null && !shopName.isEmpty()) {
                log.debug("shopName extracted from 'host' parameter: {}", shopName);
                // Store in session for future requests
                if (request.getSession() != null) {
                    request.getSession().setAttribute(AuthConstants.SHOP_ATTRIBUE_NAME, shopName);
                }
                return shopName;
            }
        }

        log.debug("Could not find shopName in any source");
        return null;
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

            String shopName = null;

            if (decodedHost.contains("admin.shopify.com/store/")) {
                // Format: admin.shopify.com/store/mystore
                String[] parts = decodedHost.split("/store/");
                if (parts.length > 1) {
                    String storePart = parts[1].split("/")[0];
                    shopName = storePart + ".myshopify.com";
                }
            } else if (decodedHost.contains(".myshopify.com")) {
                // Format: mystore.myshopify.com/admin or similar
                int startIndex = decodedHost.indexOf("://");
                if (startIndex != -1) {
                    decodedHost = decodedHost.substring(startIndex + 3);
                }
                if (decodedHost.contains("/")) {
                    shopName = decodedHost.substring(0, decodedHost.indexOf("/"));
                } else {
                    shopName = decodedHost;
                }
            }

            return shopName;
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode host parameter: {}", e.getMessage());
            return null;
        }
    }

}
