package com.justblackmagic.shopify.api.rest;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/*
 * This class is a Spring Service component which generates Shopify REST client instances with the given shop name and access token, using the API
 * version configured in shopify.api.rest.version property.
 */
@Slf4j
@Service
@Data
public class ShopifyRestClientService {

    @Value("${shopify.api.rest.version}")
    private String apiVersion = "2021-10";


    /**
     * @param shopName
     * @param accessToken
     * @return ShopifyRestClient
     */
    public ShopifyRestClient getShopifyRestClient(final String shopName, final String accessToken) {
        log.debug("getShopifyRestClient called with shopName: {}", shopName);
        log.trace("getShopifyRestClient called with accessToken: {}", accessToken);
        return ShopifyRestClient.newBuilder().withSubdomain(shopName).withAccessToken(accessToken).withApiVersion(apiVersion).build();
    }

}
