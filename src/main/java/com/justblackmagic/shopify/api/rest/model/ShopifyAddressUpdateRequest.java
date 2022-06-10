package com.justblackmagic.shopify.api.rest.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@JsonInclude(Include.ALWAYS)
public class ShopifyAddressUpdateRequest {

	@JsonProperty("first_name")
	private String firstName;
	@JsonProperty("last_name")
	private String lastname;
	private String company;
	private String address1;
	private String address2;
	private String city;
	private String zip;
	private String province;
	private String country;
	@JsonProperty("province_code")
	private String provinceCode;
	@JsonProperty("country_code")
	private String countryCode;
	private String phone;
	private BigDecimal latitude;
	private BigDecimal longitude;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}

}
