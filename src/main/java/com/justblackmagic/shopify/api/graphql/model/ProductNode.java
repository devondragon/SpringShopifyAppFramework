package com.justblackmagic.shopify.api.graphql.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductNode {
    @JsonProperty("node")
    private Product product;
}
