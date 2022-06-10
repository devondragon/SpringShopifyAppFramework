package com.justblackmagic.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyRefundRoot {

	private ShopifyRefund refund;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
