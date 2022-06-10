package com.justblackmagic.shopify.api.graphql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author justblackmagic
 * @since 0.0.1
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * GraphqlRequestBody
 */
public class GraphqlRequestBody {
    /* GraphQL query */
    private String query;

    /* GraphQL variables */
    private Object variables;
}
