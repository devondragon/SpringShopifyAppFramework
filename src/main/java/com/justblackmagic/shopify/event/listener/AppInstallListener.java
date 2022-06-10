package com.justblackmagic.shopify.event.listener;

import com.justblackmagic.shopify.api.graphql.ShopifyGraphQLClientService;
import com.justblackmagic.shopify.api.rest.ShopifyRestClient;
import com.justblackmagic.shopify.api.rest.ShopifyRestClientService;
import com.justblackmagic.shopify.api.rest.model.Webhook;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClient;
import com.justblackmagic.shopify.auth.persistence.repository.JPAAuthorizedClientRepository;
import com.justblackmagic.shopify.event.events.AppInstallEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppInstallListener {

    @Autowired
    private ShopifyRestClientService shopifyRestClientService;

    @Autowired
    ShopifyGraphQLClientService shopifyGraphQLClientService;

    @Autowired
    private JPAAuthorizedClientRepository authorizedClientRepository;

    @Value("${shopify.app.hostname}")
    private String shopifyAppHostname;


    /**
     * @param event
     */
    @Async
    @EventListener
    public void onApplicationEvent(final AppInstallEvent event) {
        log.debug("AppInstallListener.onApplicationEvent: called with event: {}", event.toString());

        // Load store name from the Shopify REST API
        if (authorizedClientRepository != null) {
            AuthorizedClient client = authorizedClientRepository.findByPrincipalName(event.getShopName());
            if (client == null) {
                log.debug("onApplicationEvent: no client was found for shop name: {}", event.getShopName());
            } else {
                log.debug("onApplicationEvent: client found for shop name: {}", event.getShopName());
                String token = client.getAccessTokenValue();
                log.trace("onApplicationEvent: token: {}", token);

                // Need to install the app uninstall webhook configuration

                String clientId = client.getClientRegistrationId();
                log.trace("onApplicationEvent: clientId: {}", clientId);
                if (clientId != null) {
                    // Create webhook setup data
                    Webhook webhook = new Webhook();
                    webhook.setAddress("https://" + shopifyAppHostname + "/webhook/uninstall?id=" + clientId);
                    webhook.setTopic("app/uninstalled");
                    webhook.setFormat("json");


                    // Call webhook creation API
                    ShopifyRestClient shopifyRestClient =
                            shopifyRestClientService.getShopifyRestClient(client.getPrincipalName(), client.getAccessTokenValue());
                    shopifyRestClient.createWebhook(webhook);

                }

            }
        }
    }
}
