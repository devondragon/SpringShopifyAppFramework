package com.justblackmagic.shopify.api.rest.model;

import java.util.LinkedList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyCustomCollectionsRoot {

	@JsonProperty("custom_collections")
	private List<ShopifyCustomCollection> customCollections = new LinkedList<ShopifyCustomCollection>();


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}

}
