package com.justblackmagic.shopify.auth;

import java.util.Arrays;
import com.justblackmagic.shopify.auth.customization.CustomRequestEntityConverter;
import com.justblackmagic.shopify.auth.customization.CustomTokenResponseConverter;
import com.justblackmagic.shopify.auth.customization.ShopifyOAuth2AuthorizationRequestResolver;
import com.justblackmagic.shopify.auth.customization.ShopifyOAuthAuthenticationSuccessHandler;
import com.justblackmagic.shopify.auth.service.JPAOAuth2AuthorizedClientService;
import com.justblackmagic.shopify.auth.service.ShopifyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Configure Spring Security to use our custom Shopify OAuth2 implementation.
 * 
 * @author justblackmagic
 */
@Slf4j
@EnableWebSecurity
public class ClientSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("#{'${shopify.security.unprotectedURIs}'.split(',')}")
	private String[] unprotectedURIsArray;

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;


	/**
	 * @param http
	 * @throws Exception
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		log.info("Configuring Spring Security...");
		log.info("Unprotected URIs: {}", Arrays.toString(unprotectedURIsArray));
		http.authorizeRequests().antMatchers(unprotectedURIsArray).permitAll().anyRequest().authenticated().and().oauth2Login()
				.successHandler(shopifyOAuthAuthenticationSuccessHandler()).authorizationEndpoint()
				.authorizationRequestResolver(new ShopifyOAuth2AuthorizationRequestResolver(this.clientRegistrationRepository)).and().and().logout()
				.logoutSuccessUrl("/").and().oauth2Login().tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient()).and()
				.userInfoEndpoint().userService(getUserService());
		http.csrf().disable();
	}


	/**
	 * @param clientRegistrationRepository
	 * @param authorizedClientRepository
	 * @return WebClient
	 */
	@Bean
	WebClient webClient(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientRepository authorizedClientRepository) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
				new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, authorizedClientRepository);
		oauth2.setDefaultOAuth2AuthorizedClient(true);
		return WebClient.builder().apply(oauth2.oauth2Configuration()).build();
	}


	/**
	 * @return OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
	 */
	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
		DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
		accessTokenResponseClient.setRequestEntityConverter(new CustomRequestEntityConverter());

		OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
		tokenResponseHttpMessageConverter.setAccessTokenResponseConverter(new CustomTokenResponseConverter());
		RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

		accessTokenResponseClient.setRestOperations(restTemplate);
		return accessTokenResponseClient;
	}


	/**
	 * @return OAuth2UserService<OAuth2UserRequest, OAuth2User>
	 */
	protected OAuth2UserService<OAuth2UserRequest, OAuth2User> getUserService() {
		return new ShopifyUserService();
	}


	/**
	 * @return OAuth2AuthorizedClientService
	 */
	@Bean
	public OAuth2AuthorizedClientService oAuth2AuthorizedClientService() {
		return new JPAOAuth2AuthorizedClientService();
	}

	@Bean
	public AuthenticationSuccessHandler shopifyOAuthAuthenticationSuccessHandler() {
		return new ShopifyOAuthAuthenticationSuccessHandler();
	}

}
