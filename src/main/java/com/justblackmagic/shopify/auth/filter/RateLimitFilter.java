package com.justblackmagic.shopify.auth.filter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Rate limiting filter to protect public endpoints from abuse.
 * Uses Bucket4j for token bucket rate limiting with Caffeine cache for automatic eviction.
 *
 * <p>The cache automatically evicts entries after 10 minutes of inactivity to prevent memory leaks.
 *
 * <p>Note: This filter uses {@link HttpServletRequest#getRemoteAddr()} to get the client IP.
 * When deployed behind a proxy, configure the proxy/container (e.g., via RemoteIpFilter or
 * Tomcat's RemoteIpValve) so that getRemoteAddr() returns the original client IP.
 */
@Slf4j
public class RateLimitFilter implements Filter {

    /** Cache with automatic eviction after 10 minutes of inactivity to prevent memory leaks */
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private int requestsPerMinute = 100;

    /**
     * Sets the maximum requests per minute per IP address.
     *
     * @param requestsPerMinute the rate limit
     */
    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);
        Bucket bucket = buckets.get(clientIp, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("Retry-After", "60");
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
        }
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Gets the client IP address from the request.
     *
     * <p>This method uses {@link HttpServletRequest#getRemoteAddr()} which returns the IP
     * address of the client or last proxy that sent the request. When deployed behind a
     * reverse proxy, configure the proxy/container to set the correct remote address
     * (e.g., using Tomcat's RemoteIpValve or Spring's ForwardedHeaderFilter).
     *
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        // Use the remote address provided by the container.
        // When behind a proxy, the container should be configured (via RemoteIpValve,
        // ForwardedHeaderFilter, etc.) to set the correct remote address.
        return request.getRemoteAddr();
    }
}
