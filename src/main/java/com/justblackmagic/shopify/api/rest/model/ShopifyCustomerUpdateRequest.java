package com.justblackmagic.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(Include.ALWAYS)
public class ShopifyCustomerUpdateRequest {

	private String id;
	private String email;
	@JsonProperty("first_name")
	private String firstName;
	@JsonProperty("last_name")
	private String lastname;
	private String phone;

	private ShopifyCustomerUpdateRequest(final Steps steps) {
		this.id = steps.id;
		this.email = steps.email;
		this.firstName = steps.firstName;
		this.lastname = steps.lastname;
		this.phone = steps.phone;
	}

	public static interface BuildStep {
		ShopifyCustomerUpdateRequest build();
	}

	public static interface PhoneStep {
		BuildStep withPhone(final String phone);
	}

	public static interface EmailStep {
		PhoneStep withEmail(final String email);
	}

	public static interface LastNameStep {
		EmailStep withLastName(final String lastName);
	}

	public static interface FirstNameStep {
		LastNameStep withFirstName(final String firstName);
	}

	public static interface IdStep {
		FirstNameStep withId(final String id);
	}


	/**
	 * @return IdStep
	 */
	public static IdStep newBuilder() {
		return new Steps();
	}

	private static class Steps implements IdStep, FirstNameStep, LastNameStep, EmailStep, PhoneStep, BuildStep {
		private String id;
		private String email;
		private String firstName;
		private String lastname;
		private String phone;

		@Override
		public ShopifyCustomerUpdateRequest build() {
			return new ShopifyCustomerUpdateRequest(this);
		}

		@Override
		public BuildStep withPhone(final String phone) {
			this.phone = phone;
			return this;
		}

		@Override
		public PhoneStep withEmail(final String email) {
			this.email = email;
			return this;
		}

		@Override
		public EmailStep withLastName(final String lastName) {
			this.lastname = lastName;
			return this;
		}

		@Override
		public LastNameStep withFirstName(final String firstName) {
			this.firstName = firstName;
			return this;
		}

		@Override
		public FirstNameStep withId(final String id) {
			this.id = id;
			return this;
		}
	}

}
