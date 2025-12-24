# Spring Shopify App Framework

A Spring Boot and Spring Security framework for building Shopify Apps using Java.

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.txt)

## Features

- **OAuth Authentication** - Complete Shopify OAuth 2.0 flow for app installation and authentication
- **REST API Client** - Full Shopify REST Admin API client (based on [ChannelApe SDK](https://github.com/ChannelApe/shopify-sdk))
- **GraphQL Support** - GraphQL client with example queries for Shopify's Admin API
- **App Bridge Frontend** - React-based embedded app UI using Shopify App Bridge and Polaris
- **GDPR Webhooks** - Pre-configured endpoints for mandatory GDPR compliance webhooks
- **Secure Token Storage** - AES-GCM encrypted access token persistence

## Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher (for building the React frontend)
- **MariaDB** or MySQL database
- **Shopify Partner Account** with a development store

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/devondragon/SpringShopifyAppFramework.git
cd SpringShopifyAppFramework
```

### 2. Configure Your App

Copy the example configuration and update with your Shopify app credentials:

```bash
cp src/main/resources/application-local-example.yml src/main/resources/application-local.yml
```

Edit `application-local.yml` with your settings:

```yaml
shopify:
  app:
    hostname: https://your-app-hostname.com
    embedded: true  # Set to false for standalone apps
  auth:
    client-id: your-shopify-client-id
    client-secret: your-shopify-client-secret
    tokenEncryptionKey: your-256-bit-aes-key-base64-encoded
```

Generate an AES encryption key at: https://www.digitalsanctuary.com/aes-key-generator-free

### 3. Set Up the Database

Configure database connection via environment variables or in your local config:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=shopifyapp
export DB_USER=your-db-user
export DB_PASSWORD=your-db-password
```

### 4. Build and Run

```bash
./gradlew bootRun -Pprofiles=local
```

The application will start at `http://localhost:8080`.

## Configuration

### Shopify API Versions

The framework uses Shopify API version `2024-10` by default. Configure in `application.yml`:

```yaml
shopify:
  api:
    graphql:
      version: 2024-10
    rest:
      version: 2024-10
```

### OAuth Scopes

Default scopes are configured in `application.yml`. Modify as needed for your app:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          shopify:
            scope: read_products,write_products,read_price_rules,write_price_rules,read_inventory,write_inventory
```

### Embedded vs Standalone Apps

Set `shopify.app.embedded` to `true` for embedded apps or `false` for standalone apps. For embedded apps, add the embedded endpoints to your unprotected URIs:

```yaml
shopify:
  app:
    embedded: true
  security:
    unprotectedURIs: /,/index.html,/favicon.ico,/error,/css/*,/js/*,/dist/*,/img/*,/dash-embedded,/embedded-auth-check,/product-list
    authSuccessPage: /dash-embedded
```

## GDPR Webhooks

Configure these mandatory webhook URLs in your Shopify Partner Dashboard:

| Webhook | URL |
|---------|-----|
| Customer Data Request | `https://your-app.com/webhook/gdpr/data-request` |
| Customer Data Erasure | `https://your-app.com/webhook/gdpr/customer-delete` |
| Shop Data Erasure | `https://your-app.com/webhook/gdpr/shop-delete` |

Implement your business logic in the controllers under `com.justblackmagic.shopify.app.controller.webhooks`.

## Project Structure

```
src/
├── main/
│   ├── java/com/justblackmagic/shopify/
│   │   ├── api/
│   │   │   ├── graphql/     # GraphQL client and models
│   │   │   └── rest/        # REST API client and models
│   │   ├── app/
│   │   │   └── controller/  # App controllers and webhook handlers
│   │   └── auth/            # OAuth and security configuration
│   ├── resources/
│   │   ├── graphql/         # GraphQL query files
│   │   └── templates/       # Thymeleaf templates
│   └── webapp/
│       └── javascript/      # React/App Bridge frontend
└── test/                    # Unit and integration tests
```

## Security Considerations

- **Never enable TRACE logging in production** - It will log sensitive data including API keys and tokens
- Access tokens are encrypted using AES-GCM before database storage
- Webhook endpoints validate HMAC signatures from Shopify
- REST API responses with unknown fields are logged at DEBUG level (search for "ShopifyRestAPI Ignored Property")

## Running Tests

```bash
./gradlew test
```

## Dependency Security Scanning

Run OWASP dependency check:

```bash
./gradlew dependencyCheckAnalyze
```

Reports are generated in `build/reports/dependency-check-report.html`.

## Documentation

- [Wiki Home](https://github.com/devondragon/SpringShopifyAppFramework/wiki)
- [Quick Start Guide](https://github.com/devondragon/SpringShopifyAppFramework/wiki/Quick-Start-Guide)
- [Shopify App Development Docs](https://shopify.dev/apps)

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## Credits

- REST API client based on [ChannelApe Shopify SDK](https://github.com/ChannelApe/shopify-sdk), modernized with Lombok and Jackson annotations for current API versions

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.txt](LICENSE.txt) file for details.
