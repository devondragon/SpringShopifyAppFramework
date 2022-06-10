package com.justblackmagic.shopify.auth.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Data;

/**
 * A Shopify Store implementation of the OAuth2User interface.
 * 
 * @author justblackmagic
 */
@Data
public class ShopifyStoreUser implements OAuth2User, Serializable {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The attribute key that holds the access token value.
	 */
	public static final String ACCESS_TOKEN_KEY = "shopify_access_token";

	/**
	 * The attribute key that holds the api key.
	 */
	public static final String API_KEY = "shopify_client_api_key";

	/**
	 * The name, in this case the full domain name of the Shopify Store.
	 */
	private String name = null;

	private Collection<? extends GrantedAuthority> authorities = null;

	private Map<String, Object> attributes = null;

	/**
	 * Create a new ShopifyStore.
	 * 
	 * @param name The full domain name
	 * @param accessToken The raw OAuth token
	 * @param apiKey The api key of this app
	 * @param authorities The authorities granted to the app
	 */
	public ShopifyStoreUser(String name, String accessToken, String apiKey, Collection<? extends GrantedAuthority> authorities) {
		this.name = name;
		this.attributes = new HashMap<>();
		this.attributes.put(ACCESS_TOKEN_KEY, accessToken);
		this.attributes.put(API_KEY, apiKey);
		this.authorities = authorities == null ? new ArrayList<>() : authorities;
	}

	/**
	 * Create a new ShopifyStore.
	 * 
	 * @param name The full domain name
	 * @param accessToken The raw OAuth token
	 * @param apiKey The api key of this app
	 * @param authorities The authorities granted to the app
	 */
	public ShopifyStoreUser(String name, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
		this.name = name;
		this.authorities = authorities != null ? authorities : new ArrayList<>();
		this.attributes = attributes != null ? attributes : new HashMap<>();
	}

	/**
	 * Get the full domain name.
	 * 
	 * @return The full domain name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Get the authorities granted to the app.
	 * 
	 * @return The authorities granted to the app
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	/**
	 * Get the attributes of the user.
	 * 
	 * @return The attributes of the user
	 */
	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

}
