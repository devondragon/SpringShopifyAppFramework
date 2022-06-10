package com.justblackmagic.shopify.auth.filter;

import com.justblackmagic.shopify.auth.util.ShopifyHMACValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter registration configuration class is where we register our filters.
 * 
 * We are doing this instead of using the @Filter annotation because we want to set URL Patterns for the filters.
 */
@Configuration
public class FilterRegistrationConfig {

    @Autowired
    private ShopifyHMACValidator shopifyHMACValidator;

    /**
     * Setting up the HMAC Validation Filter.
     * 
     * @return
     */
    @Bean
    public FilterRegistrationBean<HMACVerificationFilter> hmacFilter() {
        FilterRegistrationBean<HMACVerificationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HMACVerificationFilter());
        registrationBean.getFilter().setShopifyHMACValidator(shopifyHMACValidator);
        registrationBean.addUrlPatterns("/dash");
        registrationBean.addUrlPatterns("/login/oauth2/code/shopify");
        registrationBean.setOrder(-1010); // So it runs before the Spring Security OAuth2 Filter
        return registrationBean;
    }

    /**
     * Setting up the Shopify Shop Name Filter.
     * 
     * This filter grabs the shop name from the request and sets it in the session.
     * 
     * @return
     */
    @Bean
    public FilterRegistrationBean<ShopifyShopNameFilter> shopNameFilter() {
        FilterRegistrationBean<ShopifyShopNameFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ShopifyShopNameFilter());
        registrationBean.addUrlPatterns("/dash");
        registrationBean.addUrlPatterns("/dash-embedded");
        registrationBean.setOrder(-1000); // So it runs before the Spring Security OAuth2 Filter
        return registrationBean;
    }
}
