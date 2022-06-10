package com.justblackmagic.shopify.api.graphql.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@Data
@JsonRootName(value = "productCreate")
public class ProductCreate {
    public static String NODE_NAME = "productCreate";

    @JsonProperty("product")
    private Product product;
}

