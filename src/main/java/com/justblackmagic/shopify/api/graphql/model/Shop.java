package com.justblackmagic.shopify.api.graphql.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonRootName(value = "shop")
public class Shop extends GraphQLResponse {

    public static String NODE_NAME = "shop";

    private String name;
    private String currencyCode;
    private boolean checkoutApiSupported;
    private boolean taxesIncluded;

}
