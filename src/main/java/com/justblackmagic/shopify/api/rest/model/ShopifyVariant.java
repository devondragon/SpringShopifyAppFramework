package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.justblackmagic.shopify.api.rest.model.serializer.EscapedStringSerializer;
import com.justblackmagic.shopify.api.rest.model.serializer.InventoryPolicyDeserializer;
import com.justblackmagic.shopify.api.rest.model.serializer.InventoryPolicySerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyVariant {

	public String id;

	@JsonProperty("product_id")
	public String productId;

	@JsonSerialize(using = EscapedStringSerializer.class)
	public String title;
	public BigDecimal price;
	public String sku;
	public int position;

	@JsonProperty("inventory_policy")
	@JsonSerialize(using = InventoryPolicySerializer.class)
	@JsonDeserialize(using = InventoryPolicyDeserializer.class)
	public InventoryPolicy inventoryPolicy;

	@JsonProperty("compare_at_price")
	public BigDecimal compareAtPrice;
	@JsonProperty("fulfillment_service")
	public String fulfillmentService;
	@JsonProperty("inventory_management")
	public String inventoryManagement;
	@JsonSerialize(using = EscapedStringSerializer.class)
	public String option1;
	@JsonSerialize(using = EscapedStringSerializer.class)
	public String option2;
	@JsonSerialize(using = EscapedStringSerializer.class)
	public String option3;
	@JsonProperty("created_at")
	public Date createdAt;
	@JsonProperty("updated_at")
	public Date updatedAt;
	public boolean taxable;
	public String barcode;
	public Long grams;
	@JsonProperty("image_id")
	public String imageId;
	public double weight;
	@JsonProperty("weight_unit")
	public String weightUnit;
	@JsonProperty("inventory_item_id")
	public Object inventoryItemId;
	@JsonProperty("inventory_quantity")
	public Long inventoryQuantity;
	@JsonProperty("old_inventory_quantity")
	public int oldInventoryQuantity;
	@JsonProperty("requires_shipping")
	public boolean requiresShipping;
	@JsonProperty("admin_graphql_api_id")
	public String adminGraphqlApiId;
	public long available;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}

}
