package com.justblackmagic.shopify.auth.persistence.model;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.justblackmagic.shopify.auth.util.CryptoConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;


/**
 * JPA entity for persisting authorized OAuth2 clients
 * 
 * @author justblackmagic
 */
@Data
@Entity
@IdClass(AuthorizedClientId.class)
@EntityListeners(AuditingEntityListener.class)
public class AuthorizedClient {
	@Id
	private String clientRegistrationId;

	@Id
	private String principalName;

	private String accessTokenType;

	// We are encrypting the token here to avoid storing it in plain text in the database
	@Convert(converter = CryptoConverter.class)
	private String accessTokenValue;

	private Instant accessTokenIssuedAt;

	private Instant accessTokenExpiresAt;

	@ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
	private Set<String> accessTokenScopes;

	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

}
