package com.justblackmagic.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopifyInventoryLevelRoot {

	@JsonProperty("inventory_level")
	private ShopifyInventoryLevel inventoryLevel;

}
