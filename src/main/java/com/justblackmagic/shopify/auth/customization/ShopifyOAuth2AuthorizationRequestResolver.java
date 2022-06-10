package com.justblackmagic.shopify.auth.customization;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
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
        // Try to get the shop name from the session
        String shopName = null;
        if (request == null) {
            log.error("request is null!");
        } else if (request.getSession() == null) {
            log.error("request.getSession() is null!");
        } else if (request.getSession().getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME) == null) {
            log.error("request.getSession().getAttribute(" + AuthConstants.SHOP_ATTRIBUE_NAME + ") is null!");
        } else {
            shopName = request.getSession().getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME).toString();
            log.debug("shopName: {}", shopName);
        }
        if (shopName == null || shopName.isEmpty()) {
            if (request.getAttribute("shopName") != null) {
                shopName = request.getAttribute("shopName").toString();
                log.debug("shopName: {}", shopName);
            }
        }
        if (shopName == null || shopName.isEmpty()) {
            log.error("shopName is null or empty!");
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

}
