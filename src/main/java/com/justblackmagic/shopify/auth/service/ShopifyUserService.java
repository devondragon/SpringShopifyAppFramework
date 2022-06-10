package com.justblackmagic.shopify.auth.service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import lombok.extern.slf4j.Slf4j;

/**
 * Our Shopify User Service loads the ShopifyStoreUser object from the request and session.
 * 
 * @author justblackmagic
 */
@Slf4j
public class ShopifyUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	/**
	 * Build a ShopifyStore OAuth2User using the given OAuth2UserRequest.
	 * 
	 */
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		log.debug("ShopifyUserService.loadUser:" + "called with userRequest: " + userRequest.toString());
		Object shopName = userRequest.getAdditionalParameters().get(AuthConstants.SHOP_ATTRIBUE_NAME);
		// If we don't have a shop name on the request, try to get it from the session
		if (shopName == null || !shopName.getClass().isInstance(String.class) || ((String) shopName).isEmpty()) {
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
			HttpServletRequest request = attributes.getRequest();
			HttpSession httpSession = request.getSession(true);
			shopName = httpSession.getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME);
		}
		log.debug("loadUser:shopName: {}", shopName);
		String apiKey = userRequest.getClientRegistration().getClientId();

		Set<String> scopes = userRequest.getAccessToken().getScopes();
		Collection<GrantedAuthority> authorities = null;
		if (scopes != null) {
			authorities = scopes.stream().map(scope -> new SimpleGrantedAuthority(scope)).collect(Collectors.toList());
		}

		return new ShopifyStoreUser((String) shopName, userRequest.getAccessToken().getTokenValue(), apiKey, authorities);
	}

}
