package com.justblackmagic.shopify.api.graphql.model;

import lombok.Data;

@Data
public class Product {

    public static String NODE_NAME = "product";

    private String id;
    private String title;
    private String handle;

}
