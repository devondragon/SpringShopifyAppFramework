package com.justblackmagic.shopify.app.controller;

import java.security.Principal;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import com.justblackmagic.shopify.api.graphql.ShopifyGraphQLClientService;
import com.justblackmagic.shopify.api.rest.ShopifyRestClientService;
import com.justblackmagic.shopify.api.rest.model.ShopifyProduct;
import com.justblackmagic.shopify.auth.persistence.model.AuthorizedClient;
import com.justblackmagic.shopify.auth.persistence.repository.JPAAuthorizedClientRepository;
import com.justblackmagic.shopify.auth.service.ShopifyStoreUser;
import com.justblackmagic.shopify.auth.util.JWTUtil;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@CrossOrigin(origins = "https://admin.shopify.com", maxAge = 3600)
@RequiredArgsConstructor
public class DemoEmbeddedAppController {

    private static final String SHOPIFY_ADMIN_ORIGIN = "https://admin.shopify.com";

    @Autowired
    final private ShopifyRestClientService shopifyRestClientService;

    @Autowired
    ShopifyGraphQLClientService shopifyGraphQLClientService;

    @Autowired
    private JPAAuthorizedClientRepository authorizedClientRepository;

    @Value("${spring.security.oauth2.client.registration.shopify.scope}")
    private String shopifyScopes;

    @Value("${shopify.app.hostname:}")
    private String appHostname;

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
            if (products != null && products.size() > 2) {
                List<ShopifyProduct> twoProducts = products.subList(0, 2);
                log.debug("products: {}", twoProducts.toString());
                return ResponseEntity.ok(twoProducts);
            } else {
                return ResponseEntity.ok(products);
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    /**
     * This method gets the AuthorizedClient for this Shop, if it has already been installed/authorized. Otherwise, it returns null.
     * 
     * @param request
     * @return AuthorizedClient
     */
    public AuthorizedClient getClientFromRequest(final HttpServletRequest request) {
        for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
            String nextHeaderName = (String) e.nextElement();
            String headerValue = request.getHeader(nextHeaderName);
            log.debug("Header: {} = {}", nextHeaderName, headerValue);
        }
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

    /**
     * This method gets the AuthorizedClient for this Shop, if it has already been installed/authorized. Otherwise, it returns null.
     * 
     * @param shopname
     * @return AuthorizedClient
     */
    public AuthorizedClient getClientFromShopName(final String shopName) {
        log.debug("Shop name: {}", shopName);
        if (shopName != null) {
            AuthorizedClient client = authorizedClientRepository.findByPrincipalName(shopName);
            if (client != null) {
                return client;
            } else {
                // This isn't an error, it happens if the App has not been installed to the Shop yet.
                log.debug("No client found for shop name: {}", shopName);
            }
        }
        return null;
    }

    /**
     * This method gets the Shopify Shop name from the request. The authorization request header can either contain a JWT token, which has the shop
     * name encoded within, which we check for first, or it can contain the host name of the shop encoded in Base64, which we decode and use as the
     * shop name.
     * 
     * @param request
     * @return String
     */
    private String getShopNameFromRequest(final HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        log.debug("getShopNameFromRequest: token: {}", token);
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
                // This likely means we got passed a Based64 encoded shop name instead of a JWT token
                log.debug("MalformedJwtException. This is normal for initial auth. Going to try to get the shopname from the header value");
                byte[] decodedBytes = Base64.getDecoder().decode(token);
                shopName = new String(decodedBytes);
                log.debug("host decoded to shopName: {}", shopName);
                if (shopName != null && shopName.contains("https://")) {
                    // Need to strip off the "https://" prefix that comes in on the header value
                    shopName = shopName.replace("https://", "");
                }
                if (shopName.contains("/admin")) {
                    shopName = shopName.substring(0, shopName.indexOf("/admin"));
                    log.debug("cleaned up shopName: {}", shopName);
                } else if (shopName.contains("admin")) {
                    shopName = shopName.split("/")[2] + ".myshopify.com";
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
        String shopName = null;

        if (principal == null) {
            if (request != null && request.getAttribute("shopName") != null) {
                shopName = request.getAttribute("shopName").toString();
                log.debug("dashEmbedded: shopName from attribute: {}", shopName);
            }
        } else {
            log.debug("principal: {}", principal.toString());
            if (principal instanceof OAuth2AuthenticationToken) {
                log.debug("dashEmbedded: principal found.");
                OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) principal;
                ShopifyStoreUser user = (ShopifyStoreUser) auth.getPrincipal();
                shopName = user.getName();
            }
        }

        if (shopName != null) {
            model.addAttribute("shopName", shopName);
            setCorsAndCspHeaders(request, response, shopName);
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
        setCorsAndCspHeaders(request, response, shopName);

        AuthorizedClient client = getClientFromShopName(shopName);
        if (client == null) {
            // This is the case where the app has not been installed yet. We will redirect to the oauth2/authorization endpoint to start the
            // installation process.
            log.debug("embedded-auth-check: client is null");
            AuthCheckResponse responseObj = new AuthCheckResponse();
            responseObj.setScopes(shopifyScopes);
            responseObj.setAuthenticated(false);
            responseObj.setAuthRedirectURL("/oauth2/authorization/shopify");
            responseObj.setShopName(shopName);
            return ResponseEntity.ok(responseObj);
        } else {
            // This is the case where the app has been installed and authorized. We will return the scopes and shop name.
            model.addAttribute("shopName", client.getPrincipalName());
            AuthCheckResponse responseObj = new AuthCheckResponse();
            responseObj.setScopes(shopifyScopes);
            responseObj.setAuthenticated(true);
            responseObj.setShopName(client.getPrincipalName());
            return ResponseEntity.ok(responseObj);
        }
    }

    /**
     * Sets proper CORS and CSP headers for embedded app responses.
     * Uses dynamic origin handling based on the request's Origin header.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param shopName the shop name for CSP frame-ancestors
     */
    private void setCorsAndCspHeaders(HttpServletRequest request, HttpServletResponse response, String shopName) {
        // Set CSP frame-ancestors to allow embedding in Shopify admin and the shop's domain
        if (shopName != null && !shopName.isEmpty()) {
            response.setHeader("Content-Security-Policy", "frame-ancestors https://" + shopName + " https://admin.shopify.com;");
        } else {
            response.setHeader("Content-Security-Policy", "frame-ancestors https://admin.shopify.com;");
        }

        // CORS: Access-Control-Allow-Origin must be a single origin or *
        // Only set credentials for explicitly allowed origins
        String requestOrigin = request.getHeader("Origin");
        String allowedOrigin = null;
        boolean originAllowed = false;

        if (requestOrigin != null) {
            // Allow requests from Shopify admin or the configured app hostname
            if (requestOrigin.equals(SHOPIFY_ADMIN_ORIGIN)) {
                allowedOrigin = SHOPIFY_ADMIN_ORIGIN;
                originAllowed = true;
            } else if (appHostname != null && !appHostname.isEmpty() && requestOrigin.equals(appHostname)) {
                allowedOrigin = appHostname;
                originAllowed = true;
            }
            log.debug("Request Origin: {}, Allowed Origin: {}, Allowed: {}", requestOrigin, allowedOrigin, originAllowed);
        }

        if (originAllowed && allowedOrigin != null) {
            response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, X-Requested-With, remember-me");
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        // For disallowed or missing origins, omit CORS headers entirely (per CORS spec)
    }
}
