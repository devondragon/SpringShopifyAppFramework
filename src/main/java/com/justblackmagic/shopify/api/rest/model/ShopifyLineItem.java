package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyLineItem {

	private String id;
	@JsonProperty("variant_id")
	private String variantId;
	private String title;
	private long quantity;
	private BigDecimal price;
	private long grams;
	private String sku;
	@JsonProperty("variant_title")
	private String variantTitle;
	private String vendor;
	@JsonProperty("product_id")
	private String productId;
	@JsonProperty("requires_shipping")
	private boolean requiresShipping;
	private boolean taxable;
	@JsonProperty("gift_card")
	private boolean giftCard;
	private String name;
	@JsonProperty("variant_inventory_management")
	private String variantInventoryManagement;
	@JsonProperty("fulfillable_quantity")
	private long fulfillableQuantity;
	@JsonProperty("total_discount")
	private BigDecimal totalDiscount;
	@JsonProperty("fulfillment_status")
	private String fulfillmentStatus;
	@JsonProperty("fulfillment_service")
	private String fulfillmentService;
	@JsonProperty("tax_lines")
	private List<ShopifyTaxLine> taxLines = new LinkedList<>();


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
