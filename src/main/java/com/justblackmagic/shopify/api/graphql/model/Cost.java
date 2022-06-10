package com.justblackmagic.shopify.api.graphql.model;

import lombok.Data;

@Data
public class Cost {
    public int requestedQueryCost;
    public int actualQueryCost;
    public ThrottleStatus throttleStatus;
}
