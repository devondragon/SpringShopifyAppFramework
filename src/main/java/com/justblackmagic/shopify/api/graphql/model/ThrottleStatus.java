package com.justblackmagic.shopify.api.graphql.model;

import lombok.Data;

@Data
public class ThrottleStatus {
    public double maximumAvailable;
    public int currentlyAvailable;
    public double restoreRate;
}
