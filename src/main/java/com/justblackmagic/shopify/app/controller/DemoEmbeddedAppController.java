package com.justblackmagic.shopify.app.controller;

import java.security.Principal;
import java.util.Base64;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.justblackmagic.shopify.api.graphql.ShopifyGraphQLClientService;
import com.justblackmagic.shopify.api.rest.ShopifyRestClientService;
import com.justblackmagic.shopify.api.rest.model.ShopifyProduct;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClient;
import com.justblackmagic.shopify.auth.persistence.repository.JPAAuthorizedClientRepository;
import com.justblackmagic.shopify.auth.service.ShopifyStoreUser;
import com.justblackmagic.shopify.auth.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class DemoEmbeddedAppController {

    @Autowired
    private ShopifyRestClientService shopifyRestClientService;

    @Autowired
    ShopifyGraphQLClientService shopifyGraphQLClientService;

    @Autowired
    private JPAAuthorizedClientRepository authorizedClientRepository;

    @Value("${spring.security.oauth2.client.registration.shopify.scope}")
    private String shopifyScopes;

    @Autowired
    private JWTUtil jwtUtil;


    /**
     * @param principal
     * @param model
     * @param request
     * @param response
     * @return ResponseEntity<List<ShopifyProduct>>
     */
    @GetMapping({"/product-list"})
    public ResponseEntity<List<ShopifyProduct>> productList(Principal principal, Model model, HttpServletRequest request,
            HttpServletResponse response) {

        AuthorizedClient client = getClientFromRequest(request);

        if (client != null) {
            List<ShopifyProduct> products =
                    shopifyRestClientService.getShopifyRestClient(client.getPrincipalName(), client.getAccessTokenValue()).getProducts().values();
            List<ShopifyProduct> twoProducts = products.subList(0, 2);
            log.debug("products: {}", twoProducts.toString());
            return ResponseEntity.ok(twoProducts);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    /**
     * @param request
     * @return AuthorizedClient
     */
    public AuthorizedClient getClientFromRequest(final HttpServletRequest request) {
        // for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
        // String nextHeaderName = (String) e.nextElement();
        // String headerValue = request.getHeader(nextHeaderName);
        // log.debug("Header: {} = {}", nextHeaderName, headerValue);
        // }
        String token = request.getHeader("Authorization");
        if (token != null) {
            log.debug("Authorization: {}", token);
            String shopName = getShopNameFromRequest(request);
            log.debug("Shop name: {}", shopName);
            if (shopName != null) {

                AuthorizedClient client = authorizedClientRepository.findByPrincipalName(shopName);
                if (client != null) {
                    return client;
                } else {
                    log.error("No client found for shop name: {}", shopName);
                }
            }
        } else {
            log.error("No Authorization header found");
        }
        return null;
    }

    private String getShopNameFromRequest(final HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            log.debug("Authorization: {}", token);
            String shopName = null;
            try {
                shopName = jwtUtil.getShopForToken(token);
                if (shopName != null && shopName.contains("https://")) {
                    // Need to strip off the "https://" prefix that comes in on the header value
                    shopName = shopName.replace("https://", "");
                }
            } catch (MalformedJwtException e) {
                // This likely means we got passed the host variable instead of the token
                log.debug("MalformedJwtException. Going to try to get the client from the host");
                byte[] decodedBytes = Base64.getDecoder().decode(token);
                shopName = new String(decodedBytes);
                log.debug("host decoded to shopName: {}", shopName);
                if (shopName != null && shopName.contains("/admin")) {
                    shopName = shopName.substring(0, shopName.indexOf("/admin"));
                    log.debug("cleaned up shopName: {}", shopName);
                }
            }
            log.debug("Shop name: {}", shopName);
            return shopName;
        } else {
            log.error("No Authorization header found");
        }
        return null;
    }


    /**
     * @param principal
     * @param model
     * @param request
     * @param response
     * @return String
     */
    @GetMapping({"/dash-embedded"})
    public String dashEmbedded(Principal principal, Model model, HttpServletRequest request, HttpServletResponse response) {
        log.info("dashEmbedded()");
        log.debug("dashEmbedded: principal: {}", principal);
        if (principal == null) {
            if (request != null && request.getAttribute("shopName") != null) {
                String shopName = request.getAttribute("shopName").toString();
                log.debug("dashEmbedded: shopName: {}", shopName);
                model.addAttribute("shopName", shopName);
                response.setHeader("Content-Security-Policy", "frame-ancestors https://" + shopName + " https://admin.shopify.com;");
            }
        }
        if (principal != null) {
            log.debug("principal: {}", principal.toString());
            if (principal instanceof OAuth2AuthenticationToken) {
                log.debug("dashEmbedded: principal found.");
                OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) principal;
                ShopifyStoreUser user = (ShopifyStoreUser) auth.getPrincipal();
                String shopName = user.getName();
                model.addAttribute("shopName", shopName);
                response.setHeader("Content-Security-Policy", "frame-ancestors https://" + shopName + " https://admin.shopify.com;");
            }
        }
        return "dash-embedded";
    }


    /**
     * @param principal
     * @param model
     * @param request
     * @param response
     * @return ResponseEntity<AuthCheckResponse>
     */
    @GetMapping(value = "/embedded-auth-check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthCheckResponse> embeddedAuthCheck(Principal principal, Model model, HttpServletRequest request,
            HttpServletResponse response) {
        log.info("embeddedAuthCheck()");
        String shopName = getShopNameFromRequest(request);
        response.setHeader("Content-Security-Policy", "frame-ancestors https://" + shopName + " https://admin.shopify.com;");
        AuthorizedClient client = getClientFromRequest(request);
        if (client == null) {
            log.debug("embedded-auth-check: client is null");
            AuthCheckResponse responseObj = new AuthCheckResponse();
            responseObj.setScopes(shopifyScopes);
            responseObj.setAuthenticated(false);
            responseObj.setAuthRedirectURL("/oauth2/authorization/shopify");
            responseObj.setShopName(shopName);
            return ResponseEntity.ok(responseObj);
        } else {
            model.addAttribute("shopName", client.getPrincipalName());
            AuthCheckResponse responseObj = new AuthCheckResponse();
            responseObj.setScopes(shopifyScopes);
            responseObj.setAuthenticated(true);
            responseObj.setShopName(client.getPrincipalName());
            return ResponseEntity.ok(responseObj);
        }
    }
}
