package com.justblackmagic.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyImageRoot {

	private Image image;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}

}
