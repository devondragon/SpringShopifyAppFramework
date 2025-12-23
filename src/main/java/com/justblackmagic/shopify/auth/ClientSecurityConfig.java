package com.justblackmagic.shopify.auth;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.reactive.function.client.WebClient;
import com.justblackmagic.shopify.auth.customization.ShopifyOAuth2AuthorizationRequestResolver;
import com.justblackmagic.shopify.auth.customization.ShopifyOAuthAuthenticationSuccessHandler;
import com.justblackmagic.shopify.auth.customization.ShopifyTokenResponseClient;
import com.justblackmagic.shopify.auth.service.JPAOAuth2AuthorizedClientService;
import com.justblackmagic.shopify.auth.service.ShopifyUserService;
import lombok.extern.slf4j.Slf4j;

/**
 * Configure Spring Security to use our custom Shopify OAuth2 implementation.
 *
 * Updated for Spring Security 7.0 / Spring Boot 4.0
 *
 * @author justblackmagic
 */
@Configuration
@Slf4j
@EnableWebSecurity
public class ClientSecurityConfig {

	@Value("#{'${shopify.security.unprotectedURIs}'.split(',')}")
	private String[] unprotectedURIsArray;

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;


	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		log.info("Configuring Spring Security...");
		log.info("Unprotected URIs: {}", Arrays.toString(unprotectedURIsArray));

		http.authorizeHttpRequests((authorize) -> authorize.requestMatchers(unprotectedURIsArray).permitAll().anyRequest().authenticated());

		http.oauth2Login((o -> o.successHandler(shopifyOAuthAuthenticationSuccessHandler())
				.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
						.authorizationRequestResolver(new ShopifyOAuth2AuthorizationRequestResolver(this.clientRegistrationRepository)))
				.tokenEndpoint(tokenEndpoint -> tokenEndpoint.accessTokenResponseClient(accessTokenResponseClient()))
				.userInfoEndpoint(userInfo -> userInfo.userService(getUserService()))));

		http.logout((logout) -> logout.logoutSuccessUrl("/"));

		// Enable CSRF protection with explicit exceptions for webhooks and OAuth endpoints
		// withHttpOnlyFalse() allows JavaScript (React app) to read the token
		http.csrf((csrf) -> csrf
				.ignoringRequestMatchers(
						"/webhook/uninstall",
						"/webhook/gdpr/customer-delete",
						"/webhook/gdpr/data-request",
						"/webhook/gdpr/shop-delete",
						"/oauth2/**",
						"/login/oauth2/**")
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

		// Configure security headers
		// Note: X-Frame-Options is NOT set as it conflicts with embedded app's CSP frame-ancestors
		http.headers(headers -> headers
				.contentTypeOptions(Customizer.withDefaults())  // X-Content-Type-Options: nosniff
				.xssProtection(Customizer.withDefaults())  // X-XSS-Protection: 1; mode=block
				.httpStrictTransportSecurity(hsts -> hsts  // Only set when served over HTTPS
						.includeSubDomains(true)
						.maxAgeInSeconds(31536000))
				.referrerPolicy(referrer -> referrer
						.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));

		return http.build();
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
	 * Creates the OAuth2 access token response client using our custom Shopify implementation.
	 *
	 * @return OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
	 */
	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
		return new ShopifyTokenResponseClient();
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
