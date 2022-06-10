package com.justblackmagic.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyLocation {

	private String id;
	private String name;
	private String address1;
	private String address2;
	private String city;
	private String zip;
	private String country;
	private String phone;
	private String province;

	@JsonProperty("country_code")
	private String countryCode;

	@JsonProperty("country_name")
	private String countryName;

	@JsonProperty("province_code")
	private String provinceCode;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
