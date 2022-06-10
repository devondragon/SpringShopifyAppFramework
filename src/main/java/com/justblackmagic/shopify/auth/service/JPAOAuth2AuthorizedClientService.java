package com.justblackmagic.shopify.auth.service;

import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClient;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClientId;
import com.justblackmagic.shopify.auth.persistence.repository.JPAAuthorizedClientRepository;
import com.justblackmagic.shopify.event.events.AppInstallEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Creating a JPA based OAuth2AuthorizedClientService implementation to persist authorized OAuth2 clients information in a database. Spring Security
 * provides a database backed OAuth2AuthorizedClientService implementation, but it does not use JPA, only works with MySQL, etc... This solution is
 * simpler and more flexible.
 * 
 * @author justblackmagic
 */
@Slf4j
@Primary
@Service
public class JPAOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

	@Autowired
	private JPAAuthorizedClientRepository jpaAuthorizedClientRepository;

	protected final ClientRegistrationRepository clientRegistrationRepository = null;

	/** The event publisher. */
	@Autowired
	private ApplicationEventPublisher eventPublisher;


	/**
	 * Loads the authorized client for the given principal and client registration id.
	 * 
	 * @param clientRegistrationId
	 * @param principalName
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
		log.debug("loadAuthorizedClient({}, {})", clientRegistrationId, principalName);
		// Build the composite key ID
		AuthorizedClientId clientId = new AuthorizedClientId(clientRegistrationId, principalName);
		AuthorizedClient authorizedClient = jpaAuthorizedClientRepository.getById(clientId);


		ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId("shopify");

		OAuth2AccessToken accessToken = new OAuth2AccessToken(TokenType.BEARER, authorizedClient.getAccessTokenValue(),
				authorizedClient.getAccessTokenIssuedAt(), authorizedClient.getAccessTokenExpiresAt());

		OAuth2AuthorizedClient oAuth2AuthorizedClient = new OAuth2AuthorizedClient(clientRegistration, principalName, accessToken);
		log.debug("loadAuthorizedClient: loaded client successfully.");
		return (T) oAuth2AuthorizedClient;
	}


	/**
	 * Saves the authorized client with the given principal.
	 * 
	 * @param pAuthorizedClient
	 * @param pPrincipal
	 */
	@Override
	public void saveAuthorizedClient(OAuth2AuthorizedClient pAuthorizedClient, Authentication pPrincipal) {
		log.debug("saveAuthorizedClient({}, {})", pAuthorizedClient, pPrincipal);
		AuthorizedClient authorizedClient = new AuthorizedClient();
		authorizedClient.setClientRegistrationId(pAuthorizedClient.getClientRegistration().getClientId());
		authorizedClient.setPrincipalName(pAuthorizedClient.getPrincipalName());
		authorizedClient.setAccessTokenType(TokenType.BEARER.getValue());
		authorizedClient.setAccessTokenValue(pAuthorizedClient.getAccessToken().getTokenValue());
		authorizedClient.setAccessTokenScopes(pAuthorizedClient.getAccessToken().getScopes());
		authorizedClient.setAccessTokenIssuedAt(pAuthorizedClient.getAccessToken().getIssuedAt());
		authorizedClient.setAccessTokenExpiresAt(pAuthorizedClient.getAccessToken().getExpiresAt());
		log.trace("saveAuthorizedClient: about to save.");
		jpaAuthorizedClientRepository.save(authorizedClient);
		log.debug("saveAuthorizedClient: saved client successfully.");

		eventPublisher.publishEvent(new AppInstallEvent(pAuthorizedClient.getPrincipalName(), null, null));
	}


	/**
	 * Removes the authorized client for the given principal and client registration id.
	 * 
	 * @param pClientRegistrationId
	 * @param pPrincipalName
	 */
	@Override
	public void removeAuthorizedClient(String pClientRegistrationId, String pPrincipalName) {
		log.debug("removeAuthorizedClient: Removing authorized client for client registration id: " + pClientRegistrationId + " and principal name: "
				+ pPrincipalName);
		AuthorizedClientId clientId = new AuthorizedClientId(pClientRegistrationId, pPrincipalName);
		jpaAuthorizedClientRepository.deleteById(clientId);
		log.debug("removeAuthorizedClient: client deleted.");
	}

}
