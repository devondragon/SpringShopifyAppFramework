package com.justblackmagic.shopify.app.controller;

import lombok.Data;

@Data
public class AuthCheckResponse {
    private boolean authenticated;

    private String authRedirectURL;

    private String shopName;

    private String scopes;

}
