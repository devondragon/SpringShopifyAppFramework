package com.justblackmagic.shopify.api.graphql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justblackmagic.shopify.api.graphql.model.Extensions;
import com.justblackmagic.shopify.api.graphql.model.GraphQLResponse;
import com.justblackmagic.shopify.api.graphql.model.InputWrapper;
import com.justblackmagic.shopify.api.graphql.model.Product;
import com.justblackmagic.shopify.api.graphql.model.ProductCreate;
import com.justblackmagic.shopify.api.graphql.model.Products;
import com.justblackmagic.shopify.api.graphql.model.Shop;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

/*
 * This is a Shopify Client that uses GraphQL to retrieve data from Shopify. You should get a client object using the ShopifyGraphQLClientService
 * rather than creating your own.
 * 
 * This client has a couple simple methods to retrieve basic Shop and Product data from Shopify, and a method to create a very simple new Product. You
 * can add you own methods to this client to perform more GraphQL based actions with Shopify. At a later date this may be refactored a bit to make it
 * easy to extend, to help decouple your custom code from the core framework. However, at this point, you just pull the framework into your project,
 * and make you changes directly.
 * 
 * @author justblackmagic
 * 
 * @since 0.0.1
 */
@Slf4j
@Data
public class ShopifyGraphQLClient {
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String SHOPIFY_ACCESS_TOKEN_HEADER_NAME = "X-Shopify-Access-Token";
    private static final String DATA_NODE_NAME = "data";
    private static final String EXTENSIONS_NODE_NAME = "extensions";
    private static final String NODE_NAME_FIELD_NAME = "NODE_NAME";

    private String shopName;
    private String accessToken;
    private String apiVersion;

    private WebClient webClient;

    public ShopifyGraphQLClient(String shopName, String accessToken, String apiVersion) {
        this.shopName = shopName;
        this.accessToken = accessToken;
        this.apiVersion = apiVersion;

        HttpClient httpClient = HttpClient.create().wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        this.webClient = WebClient.builder().baseUrl("https://" + shopName + "/admin/api/" + apiVersion + "/graphql.json")
                .defaultHeader(SHOPIFY_ACCESS_TOKEN_HEADER_NAME, accessToken)
                .defaultHeader(CONTENT_TYPE_HEADER_NAME, MediaType.APPLICATION_JSON_VALUE).clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }


    /**
     * @return Shop
     */
    /*
     * A simple GraphQL query to get the basic shop information. The request is configured in the resources/graphql/getShop.graphql file.
     */
    public Shop getShop() {
        // Run the GraphQL API Call and return the JSON String response
        String jsonString = runQuery("getShop");
        // Parse the JSON String response into a Shop object
        Shop shop = handleResponse(jsonString, Shop.class);
        return shop;
    };


    /**
     * @return Products
     */
    /*
     * A simple GraphQL query to get very basic product information. The request is configured in the resources/graphql/getProducts.graphql file.
     */
    public Products getProducts() {
        String jsonString = runQuery("getProducts");
        Products products = handleResponse(jsonString, Products.class);
        return products;
    }


    /**
     * @param product
     * @return Product
     */
    /*
     * A simple GraphQL query to create a product with very basic information. The request is configured in the
     * resources/graphql/createProduct.graphql file and createProductVariables.json file.
     */
    public Product createProduct(Product product) {
        Map<String, String> variablesMap = new HashMap<String, String>();
        variablesMap.put("$titleValue", product.getTitle());

        String jsonString = runQuery("createProduct", variablesMap);
        ProductCreate createdProduct = handleResponse(jsonString, ProductCreate.class);
        if (createdProduct != null) {
            return createdProduct.getProduct();
        } else {
            log.error("Error creating product");
            return null;
        }
    }


    /**
     * @param queryFileName
     * @param variablesMap
     * @return String
     */
    /*
     * This method is used to run a GraphQL query, with an optional variables file, and return the response as a JSON String.
     */
    private String runQuery(String queryFileName, Map<String, String> variablesMap) {
        String jsonString = null;
        try {
            // Load the query from a file
            String query = GraphqlSchemaReaderUtil.getSchemaFromFileName(queryFileName);
            // Try to load variables if there are any for this query
            String variablesString = GraphqlSchemaReaderUtil.getVariablesFromFileName(queryFileName);
            log.debug("variablesString: " + variablesString);

            GraphqlRequestBody graphQLRequestBody = new GraphqlRequestBody();
            graphQLRequestBody.setQuery(query);
            // If we have a variables JSON file AND the variablesMap is not empty, then we have variables to process
            if (StringUtil.isNullOrEmpty(variablesString) == false && variablesMap != null) {
                for (Map.Entry<String, String> entry : variablesMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    variablesString = variablesString.replace(key, value);
                }
                log.debug("Variables: " + variablesString);
                InputWrapper inputWrapper = new InputWrapper(variablesString);
                graphQLRequestBody.setVariables(inputWrapper);
            }
            // Run the GraphQL API Call and return the JSON String response
            jsonString = webClient.post().bodyValue(graphQLRequestBody).retrieve().bodyToMono(String.class).block();
        } catch (IOException e) {
            log.error("IOException in runQuery!", e);
        }
        return jsonString;
    }


    /**
     * @param queryFileName
     * @return String
     */
    /*
     * This method is used to run a GraphQL query and return the response as a JSON String.
     */
    private String runQuery(String queryFileName) {
        return runQuery(queryFileName, null);
    }



    /**
     * This method is used to parse the JSON response from the GraphQL API call into a Java object. It takes the object class in as a parameter and
     * returns the fully constructed object.
     * 
     * The Shopify response has a top level "data" object that contains the actual data inside it, and an "extensions" object contains additional
     * information.
     * 
     * @param <T>
     * @param jsonString the full Shopify GraphQL response
     * @param clazz the class literal of the object to be constructed
     * @return the fully constructed object of type clazz
     * @throws IOException
     */
    private <T> T handleResponse(String jsonString, Class<T> clazz) {
        T objectToReturn = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            JsonNode errorNode = jsonNode.get("errors");
            if (errorNode != null) {
                log.error("Error in GraphQL API call: " + errorNode.toString());
                return null;
            }

            JsonNode extensionsNode = jsonNode.get(EXTENSIONS_NODE_NAME);
            Extensions extensions = null;
            if (extensionsNode != null) {
                extensions = objectMapper.readValue(extensionsNode.toString(), Extensions.class);
                log.debug("extensions: {}", extensions.toString());
            }

            JsonNode dataNode = jsonNode.get(DATA_NODE_NAME);
            log.debug("dataNode: {}", dataNode.toString());

            JsonNode objectNode = dataNode.get(clazz.getField(NODE_NAME_FIELD_NAME).get(null).toString());
            objectToReturn = objectMapper.readValue(objectNode.toString(), clazz);
            if (objectToReturn instanceof GraphQLResponse && extensions != null) {
                ((GraphQLResponse) objectToReturn).setExtensions(extensions);
                ((GraphQLResponse) objectToReturn).setExtensions(extensions);
            }
        } catch (NoSuchFieldException e) {
            log.error("NoSuchFieldException: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("SecurityException: {}", e.getMessage());
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException: {}", e.getMessage());
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
        }

        return objectToReturn;
    }

}
