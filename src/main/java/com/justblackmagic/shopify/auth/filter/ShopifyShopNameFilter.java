package com.justblackmagic.shopify.auth.filter;

import java.io.IOException;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import com.justblackmagic.shopify.auth.util.ShopifyHostUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter that grabs the Shopify shop name from the request and stores it in the session for later use.
 * Supports both direct 'shop' parameter and Base64-encoded 'host' parameter used by embedded apps.
 *
 * @author justblackmagic
 */
@Slf4j
public class ShopifyShopNameFilter implements Filter {

    /**
     * This method looks for the shop name in the request and stores it in the session.
     * It checks both the 'shop' parameter (direct) and 'host' parameter (Base64 encoded for embedded apps).
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String shopName = request.getParameter(AuthConstants.SHOP_ATTRIBUE_NAME);
        log.debug("ShopName from 'shop' parameter: {}", shopName);

        // If shop name not found directly, try to extract from Base64-encoded 'host' parameter
        // Shopify sends this for embedded apps
        if (shopName == null || shopName.isEmpty()) {
            String host = request.getParameter("host");
            if (host != null && !host.isEmpty()) {
                shopName = ShopifyHostUtils.extractShopNameFromHost(host);
                log.debug("ShopName extracted from 'host' parameter: {}", shopName);
            }
        }

        if (shopName != null && !shopName.isEmpty()) {
            httpRequest.getSession().setAttribute(AuthConstants.SHOP_ATTRIBUE_NAME, shopName);
            request.setAttribute("shopName", shopName);
            log.debug("ShopName '{}' added to session.", shopName);
        }
        chain.doFilter(request, response);
    }

}
