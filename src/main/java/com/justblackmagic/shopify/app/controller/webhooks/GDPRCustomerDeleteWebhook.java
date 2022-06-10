package com.justblackmagic.shopify.app.controller.webhooks;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class GDPRCustomerDeleteWebhook {

    @Transactional
    @PostMapping(value = "/webhook/gdpr/customer-delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uninstallApp(HttpServletRequest request, HttpServletResponse response) {
        log.debug("request: {}", request);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(request.getInputStream());
            if (jsonNode != null) {
                String shopDomain = null;
                Long shopId = null;
                if (jsonNode.get("shop_domain") != null) {
                    shopDomain = jsonNode.get("shop_domain").asText();
                }
                if (jsonNode.get("shop_id") != null) {
                    shopId = jsonNode.get("shop_id").asLong();
                }
                if (shopId != null && StringUtils.isNotBlank(shopDomain)) {
                    // Handle the Customer Delete here...
                    // Payload reference here - https://shopify.dev/apps/webhooks/configuration/mandatory-webhooks
                }
                return ResponseEntity.ok("{\"status\":\"ok\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IOException e) {
            log.error("IOException while parsing request body into JSON", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }

}
