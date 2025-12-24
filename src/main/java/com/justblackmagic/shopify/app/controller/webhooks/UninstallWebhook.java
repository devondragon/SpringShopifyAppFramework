package com.justblackmagic.shopify.app.controller.webhooks;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClient;
import com.justblackmagic.shopify.auth.persistence.repository.JPAAuthorizedClientRepository;
import com.justblackmagic.shopify.auth.util.ShopifyHMACValidator;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class UninstallWebhook {

    private final JPAAuthorizedClientRepository authorizedClientRepository;
    private final ShopifyHMACValidator shopifyHMACValidator;
    private final EntityManager entityManager;

    public UninstallWebhook(JPAAuthorizedClientRepository authorizedClientRepository, ShopifyHMACValidator shopifyHMACValidator,
            EntityManager entityManager) {
        this.authorizedClientRepository = authorizedClientRepository;
        this.shopifyHMACValidator = shopifyHMACValidator;
        this.entityManager = entityManager;
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping(value = "/webhook/uninstall", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uninstallApp(HttpServletRequest request, @RequestBody String requestBody, @RequestParam String id) {
        log.debug("request: {}", request);
        log.debug("id: {}", id);

        if (!shopifyHMACValidator.validatePostHMAC(request, requestBody)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (id == null) {
            return ResponseEntity.ok("{\"status\":\"ok\"}");
        }

        List<AuthorizedClient> clients = authorizedClientRepository.findByClientRegistrationId(id);
        clients.forEach(entityManager::remove);
        return ResponseEntity.ok("{\"message\": \"success\"}");
    }

}
