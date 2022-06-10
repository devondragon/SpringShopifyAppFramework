package com.justblackmagic.shopify.app.controller.webhooks;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClient;
import com.justblackmagic.shopify.auth.persistence.repository.JPAAuthorizedClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class UninstallWebhook {

    @Autowired
    private JPAAuthorizedClientRepository authorizedClientRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @PostMapping(value = "/webhook/uninstall", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uninstallApp(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
        log.debug("request: {}", request);
        log.debug("id: {}", id);
        if (id != null) {
            List<AuthorizedClient> clients = authorizedClientRepository.findByClientRegistrationId(id);
            for (AuthorizedClient authorizedClient : clients) {
                entityManager.remove(authorizedClient);
            }
            return ResponseEntity.ok("{\"message\": \"success\"}");
        }
        return ResponseEntity.ok("{\"status\":\"ok\"}");
    }

}
