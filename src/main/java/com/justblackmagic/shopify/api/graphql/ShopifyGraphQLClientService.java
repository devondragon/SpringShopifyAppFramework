package com.justblackmagic.shopify.api.graphql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/*
 * Spring Service component which generates GraphQL client instances with the given shop name and access token, using the API version configured in
 * shopify.api.graphql.version property.
 */
@Slf4j
@Service
@Data
public class ShopifyGraphQLClientService {


    @Value("${shopify.api.graphql.version}")
    private String apiVersion = "2021-10";


    /**
     * @param shopName
     * @param accessToken
     * @return ShopifyGraphQLClient
     */
    public ShopifyGraphQLClient getShopifyGraphQLClient(final String shopName, final String accessToken) {
        log.debug("getShopifyGraphQLClient called with shopName: {}", shopName);
        log.trace("getShopifyGraphQLClient called with accessToken: {}", accessToken);

        return new ShopifyGraphQLClient(shopName, accessToken, apiVersion);

    }

}
