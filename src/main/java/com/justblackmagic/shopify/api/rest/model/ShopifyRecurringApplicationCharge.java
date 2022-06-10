package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.justblackmagic.shopify.api.rest.model.serializer.EscapedStringSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyRecurringApplicationCharge {

	private String id;
	@JsonProperty("api_client_id")
	private String apiClientId;
	private String name;
	private String terms;
	private BigDecimal price;
	@JsonProperty("capped_amount")
	private BigDecimal cappedAmount;
	private String status;
	@JsonProperty("return_url")
	@JsonSerialize(using = EscapedStringSerializer.class)
	private String returnUrl;
	@JsonProperty("confirmation_url")
	@JsonSerialize(using = EscapedStringSerializer.class)
	private String confirmationUrl;
	@JsonProperty("trial_days")
	private int trialDays;
	@JsonProperty("trial_end_on")
	private String trialEndsOn;
	@JsonProperty("activated_on")
	private String activatedOn;
	@JsonProperty("billing_on")
	private String billingOn;
	@JsonProperty("cancelled_on")
	private String cancelledOn;
	@JsonProperty("created_at")
	private String createdAt;
	@JsonProperty("updated_on")
	private String updatedOn;
	private Boolean test;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}

}
