package com.justblackmagic.shopify.api.rest.exceptions;

import java.util.List;

import javax.ws.rs.core.Response;

import com.justblackmagic.shopify.api.rest.mappers.ResponseEntityToStringMapper;

public class ShopifyErrorResponseException extends RuntimeException {

	static final String MESSAGE = "Received unexpected Response Status Code of %d\nResponse Headers of:\n%s\nResponse Body of:\n%s";
	private final int statusCode;
	private static final long serialVersionUID = 5646635633348617058L;
	private final String responseBody;
	private final List<ShopifyErrorCode> shopifyErrorCodes;

	public ShopifyErrorResponseException(final Response response) {
		super(buildMessage(response));
		this.responseBody = ResponseEntityToStringMapper.map(response);
		this.shopifyErrorCodes = ShopifyErrorCodeFactory.create(responseBody);
		this.statusCode = response.getStatus();
	}


	/**
	 * @param response
	 * @return String
	 */
	private static String buildMessage(final Response response) {
		final String readEntity = ResponseEntityToStringMapper.map(response);
		return String.format(MESSAGE, response.getStatus(), response.getStringHeaders(), readEntity);
	}


	/**
	 * @return int
	 */
	public int getStatusCode() {
		return statusCode;
	}


	/**
	 * @return String
	 */
	public String getResponseBody() {
		return responseBody;
	}


	/**
	 * @return List<ShopifyErrorCode>
	 */
	public List<ShopifyErrorCode> getShopifyErrorCodes() {
		return shopifyErrorCodes;
	}

}
