package com.justblackmagic.shopify.api.rest.model;

public class ShopifyVariantMetafieldCreationRequest {

	private final String variantId;
	private final Metafield request;

	public static interface VariantIdStep {
		NamespaceStep withVariantId(final String variantId);
	}

	public static interface NamespaceStep {
		KeyStep withNamespace(final String namespace);
	}

	public static interface KeyStep {
		ValueStep withKey(final String key);
	}

	public static interface ValueStep {
		ValueTypeStep withValue(final String value);
	}

	public static interface ValueTypeStep {
		BuildStep withValueType(final MetafieldValueType valueType);
	}

	public static interface BuildStep {
		ShopifyVariantMetafieldCreationRequest build();
	}


	/**
	 * @return VariantIdStep
	 */
	public static VariantIdStep newBuilder() {
		return new Steps();
	}


	/**
	 * @return String
	 */
	public String getVariantId() {
		return variantId;
	}


	/**
	 * @return Metafield
	 */
	public Metafield getRequest() {
		return request;
	}

	private ShopifyVariantMetafieldCreationRequest(final Steps steps) {
		this.variantId = steps.variantId;
		this.request = steps.request;
	}

	private static class Steps implements VariantIdStep, NamespaceStep, KeyStep, ValueStep, ValueTypeStep, BuildStep {

		private String variantId;
		private Metafield request = new Metafield();

		@Override
		public ShopifyVariantMetafieldCreationRequest build() {
			return new ShopifyVariantMetafieldCreationRequest(this);
		}

		@Override
		public BuildStep withValueType(final MetafieldValueType valueType) {
			this.request.setValueType(valueType);
			return this;
		}

		@Override
		public ValueTypeStep withValue(final String value) {
			this.request.setValue(value);
			return this;
		}

		@Override
		public ValueStep withKey(final String key) {
			this.request.setKey(key);
			return this;
		}

		@Override
		public KeyStep withNamespace(final String namespace) {
			this.request.setNamespace(namespace);
			return this;
		}

		@Override
		public NamespaceStep withVariantId(final String variantId) {
			this.variantId = variantId;
			return this;
		}

	}

}
