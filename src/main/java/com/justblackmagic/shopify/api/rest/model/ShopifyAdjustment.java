package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyAdjustment {

	private String id;
	@JsonProperty("order_id")
	private String orderId;
	@JsonProperty("refund_id")
	private String refundId;
	private BigDecimal amount;
	@JsonProperty("tax_amount")
	private BigDecimal taxAmount;
	private String kind;
	private String reason;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
