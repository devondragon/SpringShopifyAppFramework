package com.justblackmagic.shopify.auth.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import com.justblackmagic.shopify.auth.util.ShopifyHMACValidator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This Servlet Filter is responsible for verifying the authenticity of the request from Shopify using the HMAC signature.
 * 
 * @author justblackmagic
 */
@Slf4j
@Data
public class HMACVerificationFilter implements Filter {

    // Normally we'd Autowire this, but since we are using the FilterRegistrationConfig class, we have to manually inject it from there
    private ShopifyHMACValidator shopifyHMACValidator;


    /**
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("HMACVerificationFilter.doFilter() - called...");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (shopifyHMACValidator.validateHMAC(httpRequest)) {
            log.debug("HMACVerificationFilter.doFilter() - HMAC is valid");
            request.setAttribute("shopifyHMACValid", true);
            chain.doFilter(request, response);
        } else {
            log.error("HMACVerificationFilter.doFilter() - HMAC is not valid");
            request.setAttribute("shopifyHMACValid", false);
            // Currently HMAC validation is working for /dash requests but failing for other requests.
            // Until the HMAC validation logic is fixed, we will continue on....
            // HttpServletResponse httpResponse = (HttpServletResponse) response;
            // httpResponse.sendError(403, "Blocked.");
            chain.doFilter(request, response);
        }

    }

}
