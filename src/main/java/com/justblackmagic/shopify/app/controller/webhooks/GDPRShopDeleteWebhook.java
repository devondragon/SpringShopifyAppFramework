package com.justblackmagic.shopify.app.controller.webhooks;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justblackmagic.shopify.auth.util.ShopifyHMACValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class GDPRShopDeleteWebhook {

    private final ShopifyHMACValidator shopifyHMACValidator;
    private final ObjectMapper objectMapper;

    public GDPRShopDeleteWebhook(ShopifyHMACValidator shopifyHMACValidator, ObjectMapper objectMapper) {
        this.shopifyHMACValidator = shopifyHMACValidator;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @PostMapping(value = "/webhook/gdpr/shop-delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> gdprShopDelete(HttpServletRequest request, @RequestBody String requestBody) {
        log.debug("request: {}", request);


        if (!shopifyHMACValidator.validatePostHMAC(request, requestBody)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(request.getInputStream());
            if (jsonNode == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            String shopDomain = jsonNode.get("shop_domain").asText();
            Long shopId = jsonNode.get("shop_id").asLong();
            if (StringUtils.isBlank(shopDomain) || shopId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Delete all Shop data for this shop from your system here.....

            return ResponseEntity.ok("{\"status\":\"ok\"}");
        } catch (IOException e) {
            log.error("IOException while parsing request body into JSON", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}


