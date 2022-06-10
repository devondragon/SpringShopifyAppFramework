package com.justblackmagic.shopify.api.rest.model;

public interface ShopifyVariantRequest {

	public ShopifyVariant getRequest();

	public String getImageSource();

	public boolean hasImageSource();

	public boolean hasChanged();

}
