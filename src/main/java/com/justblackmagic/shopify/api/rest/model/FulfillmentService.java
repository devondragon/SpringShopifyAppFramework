package com.justblackmagic.shopify.api.rest.model;

public enum FulfillmentService {

	MANUAL("manual");

	private final String value;

	private FulfillmentService(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

}
