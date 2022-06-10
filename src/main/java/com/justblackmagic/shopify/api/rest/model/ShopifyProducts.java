package com.justblackmagic.shopify.api.rest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopifyProducts {

	private final Map<String, ShopifyProduct> productIdToShopifyProduct;

	public ShopifyProducts(final List<ShopifyProduct> shopifyProducts) {
		productIdToShopifyProduct = new HashMap<>(shopifyProducts.size());
		shopifyProducts.stream().forEach(shopifyProduct -> {
			productIdToShopifyProduct.put(shopifyProduct.getId(), shopifyProduct);
		});
	}


	/**
	 * @param productId
	 * @return ShopifyProduct
	 */
	public ShopifyProduct get(final String productId) {
		return productIdToShopifyProduct.get(productId);
	}


	/**
	 * @return List<ShopifyProduct>
	 */
	public List<ShopifyProduct> values() {
		return new ArrayList<>(productIdToShopifyProduct.values());
	}


	/**
	 * @return List<ShopifyVariant>
	 */
	public List<ShopifyVariant> getVariants() {
		final Collection<ShopifyProduct> shopifyProducts = productIdToShopifyProduct.values();
		final List<ShopifyVariant> shopifyVariants = new ArrayList<>(shopifyProducts.size());
		for (ShopifyProduct shopifyProduct : shopifyProducts) {
			shopifyVariants.addAll(shopifyProduct.getVariants());
		}
		return shopifyVariants;
	}


	/**
	 * @return int
	 */
	public int size() {
		return productIdToShopifyProduct.size();
	}


	/**
	 * @param productId
	 * @return boolean
	 */
	public boolean containsKey(final String productId) {
		return productIdToShopifyProduct.containsKey(productId);
	}

}
