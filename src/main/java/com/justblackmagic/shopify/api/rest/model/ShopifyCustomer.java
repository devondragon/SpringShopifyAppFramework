package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyCustomer {

	private String id;
	private String email;
	@JsonProperty("accepts_marketing")
	private boolean acceptsMarketing;
	@JsonProperty("created_at")

	private DateTime createdAt;
	@JsonProperty("updated_at")

	private DateTime updatedAt;
	@JsonProperty("first_name")
	private String firstName;
	@JsonProperty("last_name")
	private String lastname;
	private String phone;
	@JsonProperty("orders_count")
	private long ordersCount;
	private String state;
	@JsonProperty("total_spent")
	private BigDecimal totalSpent;
	private String note;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
