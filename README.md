# Spring Shopify App Framework

## Summary
This Spring Boot and Spring Security based application provides a platform for building Shopfiy Apps using Java/Spring.  

The planned scope for this project includes:

1.  Handling OAuth authentiation/app installation with Shopify  - DONE with minor caveats
2.  A full Shopify REST API client - Imported https://github.com/ChannelApe/shopify-sdk, although many updates need to be made. Basic functionality works.
3.  An example Shopify GraphQL client - This is done with a few basic examples. Shopify's full GraphQL schema is extremely large and complex, and one of the major advantages to GraphQL is to send, and request, just the data points you need to, each GraphQL client usage is likely to be very different
4.  An App Bridge based front end - Done


## Documentation
This is a work in progress.  

Main Documenatation Home is here: [https://github.com/devondragon/SpringShopifyAppFramework/wiki](https://github.com/devondragon/SpringShopifyAppFramework/wiki)

The Quick Start Guide is here:   [https://github.com/devondragon/SpringShopifyAppFramework/wiki/Quick-Start-Guide](https://github.com/devondragon/SpringShopifyAppFramework/wiki/Quick-Start-Guide)

## Security

Warning: TRACE level logging will log secrets: API Keys, Tokens, etc... and should NEVER be activated in production or with production data!


## Notes

Unexpected fields returned from the Shopify REST API will be ignored, but if DEBUG logging is on, each will be logged prefixed by the string 
"ShopifyRestAPI Ignored Property" so you can easily search your logs and see what new properties are coming back.  
Shopify's REST API returns many properties which are NOT in their current documentation, so we wanted a flexible way to have the application not fail, but also be able to quickly identify any changes.

The default URLs for your GDPR Webhooks, as required here - https://shopify.dev/apps/webhooks/configuration/mandatory-webhooks are:

$YOUR_APP_HOSTNAME/webhook/gdpr/data-request

$YOUR_APP_HOSTNAME/webhook/gdpr/customer-delete

$YOUR_APP_HOSTNAME/webhook/gdpr/shop-delete

You will need to add actual functionality to each of these Webhook controllers under the com.justblackmagic.shopify.app.controller.webhooks. package.

## Credits

We based our REST API client on the great work by ChannelApe - https://github.com/ChannelApe/shopify-sdk, however we have rewriten most of the code there in to be compatible with the current API version "2021-10", used Lombok to remove boilerplate code, converted everything from XML library annotations to modern JSON annotations.

