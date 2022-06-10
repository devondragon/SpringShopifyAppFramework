package com.justblackmagic.shopify.api.rest.model;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Shop {
	public String address1;
	public String address2;
	public Boolean auto_configure_tax_inclusivity;
	public boolean checkout_api_supported;
	public String city;
	public String country;
	public String country_code;
	public String country_name;
	public Boolean county_taxes;
	public Date created_at;
	public String customer_email;
	public String currency;
	public String domain;
	public List<String> enabled_presentment_currencies;
	public boolean eligible_for_card_reader_giveaway;
	public boolean eligible_for_payments;
	public String email;
	public boolean finances;

	@Deprecated
	public boolean force_ssl;
	public String google_apps_domain;
	public Boolean google_apps_login_enabled;
	public boolean has_discounts;
	public boolean has_gift_cards;
	public boolean has_storefront;
	public String iana_timezone;
	public String id;
	public double latitude;
	public double longitude;
	public String money_format;
	public String money_in_emails_format;
	public String money_with_currency_format;
	public String money_with_currency_in_emails_format;
	public boolean multi_location_enabled;
	public String myshopify_domain;
	public String name;
	public boolean password_enabled;
	public String phone;
	public String plan_display_name;
	public boolean pre_launch_enabled;
	public String cookie_consent_level;
	public String plan_name;
	public String primary_locale;
	public String primary_location_id;
	public String province;
	public String province_code;
	public boolean requires_extra_payments_agreement;
	public boolean setup_required;
	public String shop_owner;
	public String source;
	public Boolean taxes_included;
	public boolean tax_shipping;
	public String timezone;
	public Date updated_at;
	public String visitor_tracking_consent_preference;
	public String weight_unit;
	public String zip;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
