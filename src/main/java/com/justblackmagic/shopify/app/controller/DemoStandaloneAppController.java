package com.justblackmagic.shopify.app.controller;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.justblackmagic.shopify.api.rest.ShopifyRestClient;
import com.justblackmagic.shopify.api.rest.ShopifyRestClientService;
import com.justblackmagic.shopify.api.rest.model.ShopifyProducts;
import com.justblackmagic.shopify.auth.service.ShopifyStoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class DemoStandaloneAppController {

    private static final String SHOPIFY_ACCESS_TOKEN = "shopify_access_token";
    @Autowired
    private ShopifyRestClientService shopifyRestClientService;


    /**
     * @param principal
     * @param model
     * @return String
     */
    @GetMapping({"/products"})
    public String dash(Principal principal, Model model) {
        log.info("products()");
        if (principal != null) {
            log.debug("principal: {}", principal.toString());
        }
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) principal;
            ShopifyStoreUser user = (ShopifyStoreUser) auth.getPrincipal();
            log.debug("user: {}", user.toString());
            ShopifyRestClient client = shopifyRestClientService.getShopifyRestClient(user.getName(), user.getAttribute(SHOPIFY_ACCESS_TOKEN));
            ShopifyProducts products = client.getProducts();
            log.debug("products: {}", products.toString());
            model.addAttribute("products", products);
        }

        return "products";
    }


    /**
     * @param principal
     * @param model
     * @param request
     * @param response
     * @return String
     */
    @GetMapping({"/dash"})
    public String dash(Principal principal, Model model, HttpServletRequest request, HttpServletResponse response) {
        log.info("dash()");
        log.debug("dash: principal: {}", principal);
        if (principal == null) {
            if (request != null && request.getAttribute("shopName") != null) {
                String shopName = request.getAttribute("shopName").toString();
                log.debug("dash: shopName: {}", shopName);
                model.addAttribute("shopName", shopName);
                response.setHeader("Content-Security-Policy", "frame-ancestors https://" + shopName + " https://admin.shopify.com;");
            }
        }
        if (principal != null) {
            log.debug("principal: {}", principal.toString());
            if (principal instanceof OAuth2AuthenticationToken) {
                log.debug("dash: principal found.");
                OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) principal;
                ShopifyStoreUser user = (ShopifyStoreUser) auth.getPrincipal();
                String shopName = user.getName();
                model.addAttribute("shopName", shopName);
                response.setHeader("Content-Security-Policy", "frame-ancestors https://" + shopName + " https://admin.shopify.com;");
            }
        }

        return "dash";
    }
}
