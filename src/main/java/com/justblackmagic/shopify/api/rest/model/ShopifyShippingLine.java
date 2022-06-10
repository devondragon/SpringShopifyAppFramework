package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyShippingLine {

	private String id;
	private String title;
	private BigDecimal price;
	private String code;
	private String source;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
