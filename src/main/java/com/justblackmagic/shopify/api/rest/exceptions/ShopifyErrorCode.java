package com.justblackmagic.shopify.api.rest.exceptions;

import java.io.Serializable;

public class ShopifyErrorCode implements Serializable {

	private static final long serialVersionUID = -3870975240510101019L;

	public enum Type {
		SHIPPING_ADDRESS, UNKNOWN
	}

	private final Type type;
	private final String message;

	public ShopifyErrorCode(final Type type, final String message) {
		this.type = type;
		this.message = message;
	}


	/**
	 * @return Type
	 */
	public Type getType() {
		return type;
	}


	/**
	 * @return String
	 */
	public String getMessage() {
		return message;
	}

}
