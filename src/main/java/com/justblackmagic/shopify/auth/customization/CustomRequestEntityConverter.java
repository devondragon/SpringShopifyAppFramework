package com.justblackmagic.shopify.auth.customization;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom implementation of {@link OAuth2AuthorizationCodeGrantRequestEntityConverter} to add custom headers of client_id and client_secret to the
 * request, as required by Shopify. It also sets the correct endpoint URL for the shop in context.
 * 
 * @author justblackmagic
 *
 */
@Slf4j
public class CustomRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {

	/** The POST param name for the Client Id to be passed to Shopify */
	private static final String CLIENT_ID_PARAM_NAME = "client_id";

	/** The POST param name for the Client Secret to be passed to Shopify */
	private static final String CLIENT_SECRET_PARAM_NAME = "client_secret";

	/**
	 * The string used as the Shopify shop hostname placeholder in configured URLs. This hostname will be dynamically replaced with the correct
	 * hostname for the Shop in context for URLs.
	 */
	private static final String SHOPNAME_REPLACE_STRING = "shopname.myshopify.com";

	/** The default converter. */
	private OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter;

	/**
	 * Basic Constructor. Sets the default converter.
	 */
	public CustomRequestEntityConverter() {
		defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
	}


	/**
	 * Custom implementation of {@link OAuth2AuthorizationCodeGrantRequestEntityConverter#convert(OAuth2AuthorizationCodeGrantRequest)} to add custom
	 * header values for Shopify. It also sets the correct endpoint URL for the shop in context.
	 * 
	 * @param request
	 * @return RequestEntity<?>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
		log.debug("CustomRequestEntityConverter.convert running....");
		// We will run the default converter first, and then add our custom headers.
		RequestEntity<?> entity = defaultConverter.convert(request);
		String clientId = request.getClientRegistration().getClientId();

		// Get the configured URL and check to see if it has the placeholder for the Shop name.
		String url = entity.getUrl().toString();
		log.debug("convert: url: {}", url);
		if (!url.contains(SHOPNAME_REPLACE_STRING)) {
			log.error("convert: Property spring.security.oauth2.client.provider.shopify.token-uri does NOT have the hostname set to "
					+ SHOPNAME_REPLACE_STRING + " which is required for proper shop hostname replacement!");
			throw new IllegalArgumentException("Property spring.security.oauth2.client.provider.shopify.token-uri does NOT have the hostname set to "
					+ SHOPNAME_REPLACE_STRING + " which is required for proper shop hostname replacement!");
		}

		// Get the shopName from the session.
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
		HttpServletRequest httpRequest = attributes.getRequest();
		HttpSession httpSession = httpRequest.getSession(true);
		String shopName = httpSession.getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME).toString();
		log.debug("convert: shopName: {}", shopName);

		// Update the URL with the shop name from the session
		String updatedUrl = url.replace(SHOPNAME_REPLACE_STRING, shopName);
		log.debug("convert: updatedUrl: {}", updatedUrl);
		URI newUrl = null;
		try {
			newUrl = new URI(updatedUrl);

		} catch (URISyntaxException e) {
			log.error("convert: URI Syntax Exception", e);
		}

		log.trace("CustomRequestEntityConverter.convert:" + "clientId: {}", clientId);

		String clientSecret = request.getClientRegistration().getClientSecret();
		log.trace("CustomRequestEntityConverter.convert:" + "clientSecret: {}", clientSecret);

		// Add the client_id and client_secret to the request. This is the Shopify App API Key and Secret.
		MultiValueMap<String, String> params = (MultiValueMap<String, String>) entity.getBody();
		params.add(CLIENT_ID_PARAM_NAME, clientId);
		params.add(CLIENT_SECRET_PARAM_NAME, clientSecret);
		return new RequestEntity<>(params, entity.getHeaders(), entity.getMethod(), newUrl);
	}

}
