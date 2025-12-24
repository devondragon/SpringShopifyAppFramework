package com.justblackmagic.shopify.auth.customization;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom token response converter. This class is used to customize the token response. Shopify returns the token but without expiration date or token
 * type, so to make it compatible with standard OAuth2 objects, we set up all the data here.
 *
 * Shopify access tokens do not expire (they are valid until the app is uninstalled), but Spring Security
 * requires an expiration value. The default is set to 2 years but can be configured via the constructor.
 *
 * @author justblackmagic
 */
@Slf4j
public class CustomTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {
	/** Default token expiration: 2 years in seconds (Shopify tokens don't actually expire) */
	private static final long DEFAULT_TOKEN_EXPIRATION_SECONDS = 63072000L; // 2 years

	private final long tokenExpirationSeconds;

	/**
	 * Creates a converter with the default token expiration (2 years).
	 */
	public CustomTokenResponseConverter() {
		this(DEFAULT_TOKEN_EXPIRATION_SECONDS);
	}

	/**
	 * Creates a converter with a custom token expiration.
	 *
	 * @param tokenExpirationSeconds the token expiration in seconds
	 */
	public CustomTokenResponseConverter(long tokenExpirationSeconds) {
		this.tokenExpirationSeconds = tokenExpirationSeconds;
	}

	/**
	 * Setups up the scopes, expiration, and token type, as the Shopify OAuth response does not return these values.
	 *
	 * @param tokenResponseParameters the token response parameters from Shopify
	 * @return OAuth2AccessTokenResponse configured for Spring Security
	 */
	@Override
	public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
		log.debug("CustomTokenResponseConverter.convert: processing token response");

		String accessToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);
		if (accessToken == null || accessToken.isEmpty()) {
			throw new OAuth2AuthorizationException(
					new OAuth2Error("invalid_token_response", "Missing access_token in Shopify response", null));
		}

		Set<String> scopes = Collections.emptySet();
		if (tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)) {
			String scope = (String) tokenResponseParameters.get(OAuth2ParameterNames.SCOPE);
			scopes = Arrays.stream(StringUtils.delimitedListToStringArray(scope, ",")).collect(Collectors.toSet());
		}

		// Use expiration from response if present, otherwise use configured default
		// Note: Shopify tokens don't actually expire, this is for Spring Security compatibility
		long expiresIn = tokenExpirationSeconds;
		if (tokenResponseParameters.containsKey(OAuth2ParameterNames.EXPIRES_IN)) {
			Object expiresInValue = tokenResponseParameters.get(OAuth2ParameterNames.EXPIRES_IN);
			if (expiresInValue != null) {
				expiresIn = Long.parseLong(expiresInValue.toString());
				log.debug("Using expires_in from response: {} seconds", expiresIn);
			}
		}

		// The token type is always "bearer"
		OAuth2AccessToken.TokenType accessTokenType = OAuth2AccessToken.TokenType.BEARER;

		return OAuth2AccessTokenResponse.withToken(accessToken).tokenType(accessTokenType).expiresIn(expiresIn).scopes(scopes).build();
	}

}
