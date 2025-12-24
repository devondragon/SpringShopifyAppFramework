package com.justblackmagic.shopify.auth.customization;

import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom OAuth2 token response client for Shopify.
 *
 * Shopify requires:
 * 1. Dynamic token endpoint URLs based on the shop name
 * 2. client_id and client_secret passed as POST parameters (not Basic Auth)
 *
 * This implementation is compatible with Spring Security 7.0 / Spring Boot 4.0.
 *
 * @author justblackmagic
 */
@Slf4j
public class ShopifyTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

	/** The POST param name for the Client Id to be passed to Shopify */
	private static final String CLIENT_ID_PARAM_NAME = "client_id";

	/** The POST param name for the Client Secret to be passed to Shopify */
	private static final String CLIENT_SECRET_PARAM_NAME = "client_secret";

	/** Placeholder for shop name in configured URLs */
	private static final String SHOPNAME_REPLACE_STRING = "shopname.myshopify.com";

	private final RestClient restClient;
	private final DefaultOAuth2TokenRequestParametersConverter<OAuth2AuthorizationCodeGrantRequest> parametersConverter;

	/**
	 * Constructor.
	 */
	public ShopifyTokenResponseClient() {
		this.restClient = RestClient.builder()
				.configureMessageConverters(builder -> {
					builder.addCustomConverter(new FormHttpMessageConverter());
					builder.addCustomConverter(new OAuth2AccessTokenResponseHttpMessageConverter());
				})
				.build();
		this.parametersConverter = new DefaultOAuth2TokenRequestParametersConverter<>();
	}

	@Override
	public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest grantRequest) {
		log.debug("ShopifyTokenResponseClient.getTokenResponse running...");

		// Build the token endpoint URI with the shop name
		URI tokenUri = buildShopifyTokenUri(grantRequest);
		log.debug("Token URI: {}", tokenUri);

		// Build the request parameters
		MultiValueMap<String, String> parameters = this.parametersConverter.convert(grantRequest);

		// Add Shopify-specific parameters
		String clientId = grantRequest.getClientRegistration().getClientId();
		String clientSecret = grantRequest.getClientRegistration().getClientSecret();
		parameters.add(CLIENT_ID_PARAM_NAME, clientId);
		parameters.add(CLIENT_SECRET_PARAM_NAME, clientSecret);

		log.trace("Request parameters: {}", parameters);

		try {
			OAuth2AccessTokenResponse response = this.restClient.post()
					.uri(tokenUri)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
					.body(parameters)
					.retrieve()
					.body(OAuth2AccessTokenResponse.class);

			if (response == null) {
				throw new OAuth2AuthorizationException(new OAuth2Error("invalid_token_response",
						"Empty response from Shopify token endpoint", null));
			}

			log.debug("Token response received successfully");
			return response;
		} catch (RestClientException ex) {
			log.error("Error exchanging authorization code for token", ex);
			throw new OAuth2AuthorizationException(new OAuth2Error("token_request_failed",
					"Failed to exchange authorization code for access token: " + ex.getMessage(), null), ex);
		}
	}

	/**
	 * Builds the Shopify token endpoint URI with the correct shop hostname.
	 *
	 * @param grantRequest the grant request
	 * @return the token endpoint URI
	 */
	private URI buildShopifyTokenUri(OAuth2AuthorizationCodeGrantRequest grantRequest) {
		String configuredUri = grantRequest.getClientRegistration().getProviderDetails().getTokenUri();
		log.debug("Configured token URI: {}", configuredUri);

		if (!configuredUri.contains(SHOPNAME_REPLACE_STRING)) {
			log.error("Property spring.security.oauth2.client.provider.shopify.token-uri does NOT have the hostname set to "
					+ SHOPNAME_REPLACE_STRING + " which is required for proper shop hostname replacement!");
			throw new IllegalArgumentException("Property spring.security.oauth2.client.provider.shopify.token-uri does NOT have the hostname set to "
					+ SHOPNAME_REPLACE_STRING + " which is required for proper shop hostname replacement!");
		}

		// Get the shop name from the session
		String shopName = getShopNameFromSession();
		log.debug("Shop name from session: {}", shopName);

		// Replace the placeholder with the actual shop name
		String updatedUri = configuredUri.replace(SHOPNAME_REPLACE_STRING, shopName);
		log.debug("Updated token URI: {}", updatedUri);

		return URI.create(updatedUri);
	}

	/**
	 * Gets the shop name from the current session.
	 *
	 * @return the shop name
	 */
	private String getShopNameFromSession() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest httpRequest = attributes.getRequest();
		HttpSession httpSession = httpRequest.getSession(true);
		Object shopAttribute = httpSession.getAttribute(AuthConstants.SHOP_ATTRIBUTE_NAME);
		if (shopAttribute == null) {
			throw new IllegalStateException("Shop name not found in session. Ensure the OAuth flow started correctly.");
		}
		return shopAttribute.toString();
	}
}
