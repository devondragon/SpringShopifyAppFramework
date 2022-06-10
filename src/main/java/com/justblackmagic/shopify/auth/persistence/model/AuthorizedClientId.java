package com.justblackmagic.shopify.auth.persistence.model;

import java.io.Serializable;

import lombok.Data;

/**
 * This class is to provide a composite (multi-column) id for the Authorized Client model's table.
 */
@Data
public class AuthorizedClientId implements Serializable {

	/**
	 * Generated SerialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String clientRegistrationId;

	private String principalName;

	public AuthorizedClientId(String clientRegistrationId, String principalName) {
		this.clientRegistrationId = clientRegistrationId;
		this.principalName = principalName;
	}

	public AuthorizedClientId() {}
}
