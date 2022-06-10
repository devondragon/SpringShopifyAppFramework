package com.justblackmagic.shopify.auth.customization;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom token response converter. This class is used to customize the token response. Shopify returns the token but without expiration date or token
 * type, so to make it compatible with standard OAuth2 objects, we set up all the data here.
 * 
 * @author justblackmagic
 */
@Slf4j
public class CustomTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {
	private static final int ONE_YEAR_IN_SECONDS = 31536000;

	private static final Set<String> TOKEN_RESPONSE_PARAMETER_NAMES = Stream.of(OAuth2ParameterNames.ACCESS_TOKEN, OAuth2ParameterNames.TOKEN_TYPE,
			OAuth2ParameterNames.EXPIRES_IN, OAuth2ParameterNames.REFRESH_TOKEN, OAuth2ParameterNames.SCOPE).collect(Collectors.toSet());


	/**
	 * Setups up the scopes, expiration, and token type, as the Shopify OAuth response does not return these values.
	 * 
	 * @param tokenResponseParameters
	 * @return OAuth2AccessTokenResponse
	 */
	@Override
	public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
		log.debug("CustomTokenResponseConverter.convert:" + "tokenResponseParameters: " + tokenResponseParameters.toString());

		String accessToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);

		Set<String> scopes = Collections.emptySet();
		if (tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)) {
			String scope = (String) tokenResponseParameters.get(OAuth2ParameterNames.SCOPE);
			scopes = Arrays.stream(StringUtils.delimitedListToStringArray(scope, ",")).collect(Collectors.toSet());
		}
		// Setting the token expiration to two years from now
		long expiresIn = Long.valueOf(ONE_YEAR_IN_SECONDS * 2);

		// The token type is always "bearer"
		OAuth2AccessToken.TokenType accessTokenType = OAuth2AccessToken.TokenType.BEARER;

		return OAuth2AccessTokenResponse.withToken(accessToken).tokenType(accessTokenType).expiresIn(expiresIn).scopes(scopes).build();
	}

}
