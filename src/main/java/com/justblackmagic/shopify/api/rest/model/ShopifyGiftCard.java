package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyGiftCard {

	private String id;
	private String note;
	@JsonProperty("api_client_id")
	private String apiClientId;
	private BigDecimal balance;
	@JsonProperty("created_at")
	private DateTime createdAt;
	@JsonProperty("initial_value")
	private BigDecimal initialValue;
	private String currency;
	@JsonProperty("customer_id")
	private String customerId;
	private String code;
	@JsonProperty("disabled_at")

	private DateTime disabledAt;
	@JsonProperty("expires_on")

	private DateTime expiresOn;
	@JsonProperty("updated_at")

	private DateTime updatedAt;
	@JsonProperty("last_characters")
	private String lastCharacters;
	@JsonProperty("line_item_id")
	private String lineItemId;
	@JsonProperty("user_id")
	private String userId;
	@JsonProperty("template_suffix")
	private String templateSuffix;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}

}
