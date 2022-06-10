package com.justblackmagic.shopify.auth.persistence.repository;

import java.util.List;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClient;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClientId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A simple JPA repository for {@link AuthorizedClient} entity.
 * 
 * @author justblackmagic
 */
public interface JPAAuthorizedClientRepository extends JpaRepository<AuthorizedClient, AuthorizedClientId> {

	/**
	 * Find the shop that matches the full shop name provided.
	 * 
	 * @param store The full shop name
	 * @return The PersistedStoreAccessToken that matches the shop name, or null if not found
	 */
	AuthorizedClient findByPrincipalName(String store);

	/**
	 * the client reg id should probably be unique but the model does not enforce it so we have to assume we could get a List of AuthorizedClients
	 * 
	 * @param clientRegistrationId the client registration id
	 * @return the list of AuthorizedClients
	 */
	List<AuthorizedClient> findByClientRegistrationId(String clientRegistrationId);

}
