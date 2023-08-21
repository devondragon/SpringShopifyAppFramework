package com.justblackmagic.shopify.auth.filter;

import java.io.IOException;
import com.justblackmagic.shopify.auth.util.AuthConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter that grabs the Shopify shop name from the request and stores it in the session for later use.
 * 
 * @author justblackmagic
 */
@Slf4j
public class ShopifyShopNameFilter implements Filter {

    /**
     * This method looks for the shop name in the request and stores it in the session.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String shopName = request.getParameter(AuthConstants.SHOP_ATTRIBUE_NAME);
        log.debug("ShopName found: {}", shopName);
        if (shopName != null) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            httpRequest.getSession().setAttribute(AuthConstants.SHOP_ATTRIBUE_NAME, shopName);
            request.setAttribute("shopName", shopName);
            log.debug("ShopName added to session.");
        }
        chain.doFilter(request, response);
    }

}
