package com.justblackmagic.shopify.auth.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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

    @Value("${shopify.auth.client-secret}")
    private String clientSecret;


    /**
     * Retrieve the shop name from the given JWT token.
     *
     * @param token JWT token string
     * @return shop name
     * @throws JwtException if the token is invalid
     */
    public String getShopForToken(final String token) throws JwtException {
        Objects.requireNonNull(token, "Token cannot be null");

        String shop = "";
        try {
            final Jws<Claims> claims = Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token);
            String shopName = (String) claims.getPayload().get("dest");
            log.debug("Parsed JWT token body: {}", claims.getPayload());
            log.debug("Extracted shop name: {}", shopName);
            shop = shopName;
        } catch (JwtException e) {
            log.error("JWT exception: {}", e.getMessage());
            throw (e);
        }
        return shop;
    }

    private SecretKey getSignInKey() {
        byte[] bytes = Base64.getDecoder().decode(clientSecret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

}
