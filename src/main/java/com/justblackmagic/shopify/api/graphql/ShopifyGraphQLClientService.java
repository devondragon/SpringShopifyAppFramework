package com.justblackmagic.shopify.api.graphql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Service component which generates GraphQL client instances with the given shop name and access token,
 * using the API version configured in shopify.api.graphql.version property.
 */
@Slf4j
@Service
@Data
public class ShopifyGraphQLClientService {

    @Value("${shopify.api.graphql.version}")
    private String apiVersion = "2021-10";

    /**
     * Enable HTTP wiretap logging for debugging. Should be false in production.
     * WARNING: Wiretap logs sensitive data including tokens and request/response bodies.
     */
    @Value("${shopify.api.graphql.wiretap.enabled:false}")
    private boolean wiretapEnabled = false;

    /**
     * Creates a ShopifyGraphQLClient for the given shop.
     *
     * @param shopName the Shopify shop name
     * @param accessToken the API access token
     * @return ShopifyGraphQLClient configured for the shop
     */
    public ShopifyGraphQLClient getShopifyGraphQLClient(final String shopName, final String accessToken) {
        log.debug("getShopifyGraphQLClient called for shop: {}", shopName);
        // Never log access tokens - they are sensitive credentials
        return new ShopifyGraphQLClient(shopName, accessToken, apiVersion, wiretapEnabled);
    }

}
