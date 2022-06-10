package com.justblackmagic.shopify.auth.persistence.model;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.justblackmagic.shopify.auth.util.CryptoConverter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

	@ElementCollection(fetch = javax.persistence.FetchType.EAGER)
	private Set<String> accessTokenScopes;

	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

}
