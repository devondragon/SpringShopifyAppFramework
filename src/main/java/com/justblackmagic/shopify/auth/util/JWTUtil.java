package com.justblackmagic.shopify.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JWTUtil {

    @Value("${shopify.auth.apiSecret}")
    private String key;


    /**
     * @param token
     * @return String
     * @throws JwtException
     */
    public String getShopForToken(final String token) throws JwtException {
        String shop = "";
        try {
            final Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key.getBytes()).build().parseClaimsJws(token);
            log.debug("Token body: " + claims.getBody().toString());
            String shopName = (String) claims.getBody().get("dest");
            log.debug("Shop name: {}", shopName);
            shop = shopName;
        } catch (JwtException e) {
            log.error("JWT exception: {}", e.getMessage());
            throw (e);
        }
        return shop;
    }


}
