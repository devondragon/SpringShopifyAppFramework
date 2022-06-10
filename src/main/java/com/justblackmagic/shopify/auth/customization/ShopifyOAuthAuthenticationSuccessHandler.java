package com.justblackmagic.shopify.auth.customization;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.justblackmagic.shopify.auth.service.ShopifyStoreUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShopifyOAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${shopify.auth.apiKey}")
    private String shopifyAPIKey;

    @Value("${shopify.security.authSuccessPage}")
    private String authSuccessPage;

    @Value("${shopify.app.embedded}")
    private boolean embedded;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public ShopifyOAuthAuthenticationSuccessHandler() {
        super();
    }


    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication) throws IOException {
        handle(request, response, authentication);
        clearAuthenticationAttributes(request);
    }

    protected void handle(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication) throws IOException {
        final String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(final Authentication authentication) {

        if (embedded) {
            if (authentication.getPrincipal() instanceof ShopifyStoreUser) {
                final ShopifyStoreUser userDetails = (ShopifyStoreUser) authentication.getPrincipal();
                final String shopName = userDetails.getName();
                log.debug("shopName: {}", shopName);

                final String redirectUrl = "https://" + shopName + "/admin/apps/" + shopifyAPIKey + authSuccessPage;
                return redirectUrl;
            }

            throw new IllegalStateException();
        } else {
            return authSuccessPage;
        }
    }

    /**
     * Removes temporary authentication-related data which may have been stored in the session during the authentication process.
     */
    protected final void clearAuthenticationAttributes(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

}
