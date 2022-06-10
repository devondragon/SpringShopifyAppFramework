package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.justblackmagic.shopify.api.rest.model.serializer.OrderRiskRecommendationDeserializer;
import com.justblackmagic.shopify.api.rest.model.serializer.OrderRiskRecommendationSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyOrderRisk {

	private String id;
	@JsonProperty("order_id")
	private String orderId;
	@JsonProperty("checkout_id")
	private String checkoutId;
	private String source;
	private BigDecimal score;
	@JsonSerialize(using = OrderRiskRecommendationSerializer.class)
	@JsonDeserialize(using = OrderRiskRecommendationDeserializer.class)
	private OrderRiskRecommendation recommendation;
	private boolean display;
	@JsonProperty("cause_cancel")
	private boolean causeCancel;
	private String message;
	@JsonProperty("merchant_message")
	private String merchantMessage;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
