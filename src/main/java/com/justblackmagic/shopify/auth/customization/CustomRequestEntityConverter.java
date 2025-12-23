package com.justblackmagic.shopify.auth.customization;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom implementation of OAuth2 token request parameters converter to add custom parameters
 * of client_id and client_secret to the request, as required by Shopify.
 *
 * Updated for Spring Security 7.0 - replaces the deprecated OAuth2AuthorizationCodeGrantRequestEntityConverter
 *
 * @author justblackmagic
 *
 */
@Slf4j
public class CustomRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> {

	/** The POST param name for the Client Id to be passed to Shopify */
	private static final String CLIENT_ID_PARAM_NAME = "client_id";

	/** The POST param name for the Client Secret to be passed to Shopify */
	private static final String CLIENT_SECRET_PARAM_NAME = "client_secret";

	/** The default converter. */
	private final DefaultOAuth2TokenRequestParametersConverter<OAuth2AuthorizationCodeGrantRequest> defaultConverter;

	/**
	 * Basic Constructor. Sets the default converter.
	 */
	public CustomRequestEntityConverter() {
		defaultConverter = new DefaultOAuth2TokenRequestParametersConverter<>();
	}


	/**
	 * Custom implementation to add custom parameter values for Shopify.
	 * Adds client_id and client_secret to the request parameters.
	 *
	 * @param request the OAuth2AuthorizationCodeGrantRequest
	 * @return MultiValueMap containing the request parameters
	 */
	@Override
	public MultiValueMap<String, String> convert(OAuth2AuthorizationCodeGrantRequest request) {
		log.debug("CustomRequestEntityConverter.convert running....");

		// Get the default parameters first
		MultiValueMap<String, String> params = defaultConverter.convert(request);

		String clientId = request.getClientRegistration().getClientId();
		log.trace("CustomRequestEntityConverter.convert: clientId: {}", clientId);

		String clientSecret = request.getClientRegistration().getClientSecret();
		log.trace("CustomRequestEntityConverter.convert: clientSecret: {}", clientSecret);

		// Add the client_id and client_secret to the request. This is the Shopify App API Key and Secret.
		params.add(CLIENT_ID_PARAM_NAME, clientId);
		params.add(CLIENT_SECRET_PARAM_NAME, clientSecret);

		return params;
	}

	/**
	 * Gets the shop name from the current session.
	 * This can be used by other components that need to resolve the shop context.
	 *
	 * @return the shop name from session
	 */
	public String getShopNameFromSession() {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
		HttpServletRequest httpRequest = attributes.getRequest();
		HttpSession httpSession = httpRequest.getSession(true);
		return httpSession.getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME).toString();
	}

}
