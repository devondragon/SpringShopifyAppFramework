# Security and Code Quality Fixes

This document tracks identified security vulnerabilities, code quality issues, and recommended improvements for the Spring Shopify App Framework.

**Audit Date:** 2025-12-23
**Related Issue:** #38 (Embedded App Installation 404 Error) - Fixed in branch `fix/issue-38-embedded-app-installation`

---

## Summary

| Severity | Count | Status |
|----------|-------|--------|
| Critical | 4 | Pending |
| High | 4 | Pending |
| Medium | 10 | Pending |
| Low | 4 | Pending |

---

## Critical Issues

### 1. HMAC Validation Bypass

**File:** `src/main/java/com/justblackmagic/shopify/auth/filter/HMACVerificationFilter.java`
**Lines:** 38-49

**Issue:** HMAC validation failures are silently ignored, allowing potentially malicious requests to pass through.

```java
// Current code - BYPASSED!
if (shopifyHMACValidator.validateHMAC(httpRequest)) {
    log.debug("HMACVerificationFilter.doFilter() - HMAC is valid");
    request.setAttribute("shopifyHMACValid", true);
    chain.doFilter(request, response);
} else {
    log.error("HMACVerificationFilter.doFilter() - HMAC is not valid");
    request.setAttribute("shopifyHMACValid", false);
    // Currently HMAC validation is working for /dash requests but failing for other requests.
    // Until the HMAC validation logic is fixed, we will continue on....
    chain.doFilter(request, response);  // ← SHOULD BLOCK!
}
```

**Risk:** Attackers can send forged webhooks or requests without valid HMAC signatures.

**Fix:**
```java
} else {
    log.error("HMACVerificationFilter.doFilter() - HMAC is not valid");
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid HMAC signature");
    return; // Don't continue the filter chain
}
```

**Additional Steps:**
- Fix the underlying HMAC validation logic for all endpoints
- Add unit tests to verify HMAC validation
- Consider a configuration flag to enforce validation when ready

---

### 2. Weak Encryption Algorithm (ECB Mode)

**File:** `src/main/java/com/justblackmagic/shopify/auth/util/CryptoConverter.java`
**Line:** 24

**Issue:** Using `AES/ECB/PKCS5Padding` which is cryptographically broken. ECB mode produces identical ciphertext for identical plaintext blocks, enabling pattern analysis attacks.

```java
// Current - INSECURE
private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
```

**Fix:**
```java
// Use authenticated encryption
private static final String ALGORITHM = "AES/GCM/NoPadding";
private static final int GCM_IV_LENGTH = 12;
private static final int GCM_TAG_LENGTH = 128;
```

**Note:** This requires updating the encryption/decryption logic to handle IV generation and storage. Existing encrypted tokens will need migration.

---

### 3. CSRF Protection Disabled Globally

**File:** `src/main/java/com/justblackmagic/shopify/auth/ClientSecurityConfig.java`
**Line:** 62

**Issue:** CSRF protection is completely disabled, making the application vulnerable to cross-site request forgery attacks.

```java
http.csrf((csrf) -> csrf.disable());
```

**Fix:**
```java
http.csrf((csrf) -> csrf
    .ignoringRequestMatchers("/webhook/**", "/oauth2/**", "/login/oauth2/**")
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
);
```

---

### 4. Outdated Shopify API Version

**File:** `src/main/resources/application.yml`
**Lines:** 14-18

**Issue:** Using API version `2021-10` which is over 3 years old. Missing security patches, deprecated endpoints, and new features.

```yaml
shopify:
  api:
    graphql:
      version: 2021-10  # OBSOLETE
    rest:
      version: 2021-10  # OBSOLETE
```

**Fix:**
```yaml
shopify:
  api:
    graphql:
      version: 2024-10  # Or latest stable
    rest:
      version: 2024-10  # Or latest stable
```

**Note:** Review [Shopify API changelog](https://shopify.dev/docs/api/release-notes) for breaking changes before updating.

---

## High Severity Issues

### 5. CSP Header Injection via Shop Name

**Files:**
- `src/main/java/com/justblackmagic/shopify/app/controller/DemoEmbeddedAppController.java:266`
- `src/main/java/com/justblackmagic/shopify/app/controller/DemoStandaloneAppController.java:67`

**Issue:** Shop name is used directly in Content-Security-Policy header without validation.

```java
response.setHeader("Content-Security-Policy", "frame-ancestors https://" + shopName + " https://admin.shopify.com;");
```

**Risk:** Malicious shop names could inject CSP directives.

**Fix:** Add shop name validation utility:
```java
public class ShopifyValidation {
    private static final Pattern SHOP_NAME_PATTERN =
        Pattern.compile("^[a-z0-9][a-z0-9\\-]*\\.myshopify\\.com$");

    public static boolean isValidShopName(String shopName) {
        return shopName != null && SHOP_NAME_PATTERN.matcher(shopName).matches();
    }

    public static String sanitizeShopName(String shopName) {
        if (!isValidShopName(shopName)) {
            throw new IllegalArgumentException("Invalid shop name format");
        }
        return shopName;
    }
}
```

---

### 6. Null Pointer Dereference Risks

**File:** `src/main/java/com/justblackmagic/shopify/auth/customization/CustomTokenResponseConverter.java`
**Line:** 42

```java
String accessToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);
// Used later without null check
```

**Fix:**
```java
String accessToken = (String) tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);
if (accessToken == null || accessToken.isEmpty()) {
    throw new OAuth2AuthorizationException(new OAuth2Error("invalid_token_response",
        "Missing access_token in Shopify response", null));
}
```

**File:** `src/main/java/com/justblackmagic/shopify/auth/customization/CustomRequestEntityConverter.java`
**Line:** 82

```java
return httpSession.getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME).toString();
```

**Fix:**
```java
Object shopAttribute = httpSession.getAttribute(AuthConstants.SHOP_ATTRIBUE_NAME);
if (shopAttribute == null) {
    throw new IllegalStateException("Shop name not found in session");
}
return shopAttribute.toString();
```

---

### 7. Unsafe Base64 Decoding

**File:** `src/main/java/com/justblackmagic/shopify/app/controller/DemoEmbeddedAppController.java`
**Line:** 154-156

**Issue:** Base64 decoding without proper error handling and no charset specification.

```java
byte[] decodedBytes = Base64.getDecoder().decode(token);
shopName = new String(decodedBytes);  // No charset specified
```

**Fix:**
```java
try {
    byte[] decodedBytes = Base64.getDecoder().decode(token);
    shopName = new String(decodedBytes, StandardCharsets.UTF_8);
} catch (IllegalArgumentException e) {
    log.warn("Invalid Base64 encoded token: {}", e.getMessage());
    return null;
}
```

---

### 8. Typo in Constants

**File:** `src/main/java/com/justblackmagic/shopify/auth/util/AuthConstants.java`
**Line:** 5

```java
public static final String SHOP_ATTRIBUE_NAME = "shop";  // Typo: ATTRIBUE
```

**Fix:**
```java
public static final String SHOP_ATTRIBUTE_NAME = "shop";

// For backwards compatibility during migration:
@Deprecated
public static final String SHOP_ATTRIBUE_NAME = SHOP_ATTRIBUTE_NAME;
```

**Note:** Requires updating all references across the codebase.

---

## Medium Severity Issues

### 9. Sensitive Data Logging

**Files:**
- `src/main/java/com/justblackmagic/shopify/auth/util/ShopifyHMACValidator.java:65`
- `src/main/java/com/justblackmagic/shopify/api/rest/ShopifyRestClientService.java:29`
- `src/main/java/com/justblackmagic/shopify/api/graphql/ShopifyGraphQLClientService.java:29`

**Issue:** Secrets and access tokens logged even at TRACE level.

```java
log.trace("Secret: {}", secret);  // NEVER log secrets
log.debug("accessToken: {}", accessToken);  // NEVER log tokens
```

**Fix:**
```java
log.trace("Validating HMAC with configured secret");
log.debug("Using access token for shop: {}", shopName);  // Log shop, not token
```

---

### 10. Missing Security Headers

**Issue:** No standard security headers configured.

**Fix:** Add a security headers filter:

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        filterChain.doFilter(request, response);
    }
}
```

---

### 11. DDL Auto Update in Production Risk

**File:** `src/main/resources/application.yml`
**Line:** 48-49

```yaml
jpa:
  hibernate:
    ddl-auto: update  # Dangerous in production
```

**Fix:**
```yaml
jpa:
  hibernate:
    ddl-auto: ${JPA_DDL_AUTO:validate}  # Default to validate, override in dev
```

Use Flyway or Liquibase for production database migrations.

---

### 12. No Rate Limiting

**Issue:** Public endpoints have no throttling protection.

**Affected Endpoints:**
- `/embedded-auth-check`
- `/dash-embedded`
- `/product-list`

**Fix:** Add rate limiting with Bucket4j or Spring Cloud Gateway:

```java
@Bean
public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
    FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new RateLimitFilter());
    registrationBean.addUrlPatterns("/embedded-auth-check", "/product-list");
    return registrationBean;
}
```

---

### 13. Overly Permissive Unprotected URIs

**File:** `src/main/resources/application.yml`
**Line:** 27-28

```yaml
unprotectedURIs: /,/index.html,/favicon.ico,/error,/css/*,/js/*,/dist/*,/img/*,/webhook/*
```

**Fix:** Be specific about webhook endpoints:
```yaml
unprotectedURIs: /,/index.html,/favicon.ico,/error,/css/*,/js/*,/dist/*,/img/*,/webhook/uninstall,/webhook/gdpr/customer-delete,/webhook/gdpr/data-request,/webhook/gdpr/shop-delete
```

---

### 14. Hardcoded Token Expiration

**File:** `src/main/java/com/justblackmagic/shopify/auth/customization/CustomTokenResponseConverter.java`
**Lines:** 26, 50

```java
private static final int ONE_YEAR_IN_SECONDS = 31536000;
long expiresIn = Long.valueOf(ONE_YEAR_IN_SECONDS * 2);  // 2 years hardcoded
```

**Fix:**
```java
// Read from response if available, otherwise use configured default
Long expiresIn = tokenResponseParameters.containsKey("expires_in")
    ? Long.valueOf(tokenResponseParameters.get("expires_in").toString())
    : Long.valueOf(shopifyTokenExpirationSeconds);  // From config
```

---

### 15. HMAC Validation Incomplete for POST Requests

**File:** `src/main/java/com/justblackmagic/shopify/auth/util/ShopifyHMACValidator.java`
**Lines:** 74-77

**Issue:** Mixed validation approaches for GET vs POST requests.

**Fix:** Consolidate into a single validation method that handles both:
```java
public boolean validateHMAC(HttpServletRequest request) {
    if ("GET".equalsIgnoreCase(request.getMethod())) {
        return validateGetHMAC(request);
    } else if ("POST".equalsIgnoreCase(request.getMethod())) {
        return validatePostHMAC(request);
    }
    log.warn("Unsupported HTTP method for HMAC validation: {}", request.getMethod());
    return false;
}
```

---

### 16. Debug Wiretap in Production Risk

**File:** `src/main/java/com/justblackmagic/shopify/api/graphql/ShopifyGraphQLClient.java`
**Line:** 58

```java
HttpClient httpClient = HttpClient.create()
    .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
```

**Fix:**
```java
HttpClient httpClient = HttpClient.create();
if (log.isTraceEnabled() && !isProductionEnvironment()) {
    httpClient = httpClient.wiretap("reactor.netty.http.client.HttpClient",
        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
}
```

---

### 17. Missing Transaction Rollback Configuration

**Files:**
- `src/main/java/com/justblackmagic/shopify/app/controller/webhooks/UninstallWebhook.java`
- `src/main/java/com/justblackmagic/shopify/app/controller/webhooks/GDPRDataRequestWebhook.java`
- `src/main/java/com/justblackmagic/shopify/app/controller/webhooks/GDPRShopDeleteWebhook.java`

**Fix:**
```java
@Transactional(rollbackFor = Exception.class)
public ResponseEntity<String> handleWebhook(...) {
    // ...
}
```

---

### 18. Session Cookie Security

**File:** `src/main/resources/application-local-example.yml`
**Lines:** 23-26

```yaml
server:
  servlet:
    session:
      cookie.secure: false  # Only for local dev
```

**Fix for production:**
```yaml
server:
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: strict
```

---

## Low Severity Issues

### 19. Missing Environment Variable Configuration

**File:** `src/main/resources/application.yml`

**Issue:** Database credentials hardcoded.

**Fix:**
```yaml
spring:
  datasource:
    url: jdbc:mariadb://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:shopifytest}
    username: ${DB_USER:shopifytest}
    password: ${DB_PASSWORD}
```

---

### 20. Production Configuration Gaps

**File:** `src/main/resources/application-prd.yml`

**Missing:**
```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

---

### 21. Missing Request Body Size Limits

**Fix in application.yml:**
```yaml
server:
  tomcat:
    max-http-post-size: 1MB
spring:
  servlet:
    multipart:
      max-file-size: 1MB
      max-request-size: 1MB
```

---

### 22. No Dependency Vulnerability Scanning

**Fix in build.gradle:**
```gradle
plugins {
    id 'org.owasp.dependencycheck' version '9.0.0'
}

dependencyCheck {
    failBuildOnCVSS = 7
    suppressionFile = 'dependency-check-suppressions.xml'
}
```

Run with: `./gradlew dependencyCheckAnalyze`

---

## Action Plan

### Phase 1 - Critical (Immediate)
- [ ] Fix HMAC validation bypass
- [ ] Update encryption from ECB to GCM
- [ ] Configure CSRF protection properly
- [ ] Update Shopify API version to 2024-10

### Phase 2 - High (Within 1 week)
- [ ] Add shop name validation
- [ ] Fix null pointer dereference issues
- [ ] Remove sensitive data logging
- [ ] Fix typo in AuthConstants

### Phase 3 - Medium (Within 2 weeks)
- [ ] Add security headers filter
- [ ] Implement rate limiting
- [ ] Configure DDL properly for production
- [ ] Specify webhook endpoints explicitly
- [ ] Add transaction rollback configuration

### Phase 4 - Low (Within 1 month)
- [ ] Move to environment variables for secrets
- [ ] Add production configuration
- [ ] Add request body size limits
- [ ] Set up dependency vulnerability scanning

---

## Completed Fixes

### Issue #38 - Embedded App Installation 404 Error
**Branch:** `fix/issue-38-embedded-app-installation`
**Status:** Fixed
**Commit:** `e67992a`

**Changes:**
- `ShopifyShopNameFilter.java` - Added Base64 host parameter extraction
- `ShopifyOAuth2AuthorizationRequestResolver.java` - Multi-source shop name lookup
- `DemoEmbeddedAppController.java` - Fixed malformed CORS headers
- `MyApp.jsx` - Fixed async auth flow race condition
- `application.yml` - Added documentation for embedded app endpoints
