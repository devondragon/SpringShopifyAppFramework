package com.justblackmagic.shopify.api.rest.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.justblackmagic.shopify.api.rest.model.serializer.EscapedStringSerializer;
import com.justblackmagic.shopify.api.rest.model.serializer.EscapedStringsSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Option {

	public String id;
	@JsonProperty("product_id")
	public String productId;
	@JsonSerialize(using = EscapedStringSerializer.class)
	public String name;
	public int position;
	@JsonSerialize(using = EscapedStringsSerializer.class)
	public List<String> values;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
