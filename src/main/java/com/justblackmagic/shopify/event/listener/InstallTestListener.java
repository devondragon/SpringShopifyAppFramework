package com.justblackmagic.shopify.event.listener;

import com.justblackmagic.shopify.api.graphql.ShopifyGraphQLClient;
import com.justblackmagic.shopify.api.graphql.ShopifyGraphQLClientService;
import com.justblackmagic.shopify.api.graphql.model.Product;
import com.justblackmagic.shopify.api.graphql.model.Products;
import com.justblackmagic.shopify.api.rest.ShopifyRestClient;
import com.justblackmagic.shopify.api.rest.ShopifyRestClientService;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClient;
import com.justblackmagic.shopify.auth.persistence.repository.JPAAuthorizedClientRepository;
import com.justblackmagic.shopify.event.events.AppInstallEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InstallTestListener {

    @Autowired
    private ShopifyRestClientService shopifyRestClientService;

    @Autowired
    ShopifyGraphQLClientService shopifyGraphQLClientService;

    @Autowired
    private JPAAuthorizedClientRepository authorizedClientRepository;

    @Value("${shopify.test.storeName}")
    private String testStoreName;


    /**
     * @param event
     */
    @Async
    @EventListener
    public void onApplicationEvent(final AppInstallEvent event) {
        log.debug("InstallTestListener.onApplicationEvent: called with event: {}", event.toString());

        // Load store name from the Shopify REST API
        if (authorizedClientRepository != null) {
            AuthorizedClient client = authorizedClientRepository.findByPrincipalName(event.getShopName());
            if (client == null) {
                log.debug("onApplicationEvent: no client was found for shop name: {}", event.getShopName());
            } else {
                log.debug("onApplicationEvent: client found for shop name: {}", event.getShopName());
                String token = client.getAccessTokenValue();
                log.trace("onApplicationEvent: token: {}", token);

                String loadedShopName = shopifyRestClientService.getShopifyRestClient(event.getShopName(), token).getShop().getShop().getName();
                log.debug("onApplicationEvent: loaded shop name: {}", loadedShopName);
            }
        }
    }

    /** This is a testing method to help test APIs on application startup. */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Context Refreshed Event");
        AuthorizedClient client = authorizedClientRepository.findByPrincipalName(testStoreName);
        if (client == null || client.getPrincipalName() == null || client.getPrincipalName().trim().isEmpty()) {
            log.info("No Test Store Oauth Client found. Will not run API tests.");
        } else {
            log.info("Test Store OAuth Client found, running tests.");
            log.info("Running REST API Tests...");

            ShopifyRestClient shopifyRestClient =
                    shopifyRestClientService.getShopifyRestClient(client.getPrincipalName(), client.getAccessTokenValue());

            String loadedShopName = shopifyRestClient.getShop().getShop().getName();
            log.debug("onApplicationEvent: loaded shop name: {}", loadedShopName);

            shopifyRestClient.getCustomCollections().forEach(customCollection -> {
                log.debug("onApplicationEvent: custom collection: {}", customCollection.getTitle());
            });

            shopifyRestClient.getProducts().values().forEach(product -> {
                log.debug("onApplicationEvent: product: {}", product.getTitle());
            });

            if (client.getAccessTokenScopes().contains("read_orders")) {
                shopifyRestClient.getOrders().forEach(order -> {
                    log.debug("onApplicationEvent: order: {}", order.getName());
                });
            }



            log.info("REST API Tests Complete.");
            log.info("Running GraphQL API Tests....");

            ShopifyGraphQLClient graphQLClient =
                    shopifyGraphQLClientService.getShopifyGraphQLClient(client.getPrincipalName(), client.getAccessTokenValue());
            String graphQLShopName = graphQLClient.getShop().getName();
            log.debug("GraphQL Shop Name: {}", graphQLShopName);

            Products products = graphQLClient.getProducts();
            products.getProducts().forEach(product -> {
                log.debug("onApplicationEvent: product: {}", product.getTitle());
            });

            if (client.getAccessTokenScopes().contains("write_products")) {
                Product product = new Product();
                product.setTitle("Test Product");
                Product newProduct = graphQLClient.createProduct(product);
                if (newProduct == null) {
                    log.debug("Error! newProduct is null!");
                } else {
                    log.debug("new Product id: {}", newProduct.getId());
                    log.debug("new Product title: {}", newProduct.getTitle());
                }
            }

            log.info("GraphQL API Tests Complete.");
        }


    }
}
