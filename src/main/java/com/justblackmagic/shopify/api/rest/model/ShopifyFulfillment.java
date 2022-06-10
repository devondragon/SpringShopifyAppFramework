package com.justblackmagic.shopify.api.rest.model;

import java.util.LinkedList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyFulfillment {

	public enum Status {

		PENDING("pending"), OPEN("open"), SUCCESS("success"), CANCELLED("cancelled"), ERROR("error"), FAILURE("failure");

		static final String NO_MATCHING_ENUMS_ERROR_MESSAGE = "No matching enum found for status: %s";
		private final String value;

		private Status(final String value) {
			this.value = value;
		}

		public static Status toEnum(final String value) {
			if (PENDING.toString().equals(value)) {
				return Status.PENDING;
			} else if (OPEN.toString().equals(value)) {
				return Status.OPEN;
			} else if (SUCCESS.toString().equals(value)) {
				return Status.SUCCESS;
			} else if (CANCELLED.toString().equals(value)) {
				return Status.CANCELLED;
			} else if (ERROR.toString().equals(value)) {
				return Status.ERROR;
			} else if (FAILURE.toString().equals(value)) {
				return Status.FAILURE;
			}

			throw new IllegalArgumentException(String.format(NO_MATCHING_ENUMS_ERROR_MESSAGE, value));
		}

		@Override
		public String toString() {
			return value;
		}
	}

	private String id;
	@JsonProperty("order_id")
	private String orderId;
	private String status;
	@JsonProperty("created_at")

	private DateTime createdAt;
	@JsonProperty("updated_at")

	private DateTime updatedAt;
	@JsonProperty("tracking_company")
	private String trackingCompany;
	@JsonProperty("tracking_number")
	private String trackingNumber;
	@JsonProperty("notify_customer")
	private boolean notifyCustomer;
	@JsonProperty("line_items")
	private List<ShopifyLineItem> lineItems = new LinkedList<>();
	@JsonProperty("tracking_url")
	private String trackingUrl;
	@JsonProperty("tracking_urls")
	private List<String> trackingUrls = new LinkedList<>();
	@JsonProperty("location_id")
	private String locationId;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
