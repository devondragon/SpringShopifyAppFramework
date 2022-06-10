package com.justblackmagic.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.joda.time.DateTime;
import org.springframework.data.annotation.ReadOnlyProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@JsonTypeName(value = "webhook")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class Webhook {

    private String topic;

    private String address;

    private String format;

    private String[] fields;

    private String[] metafieldNamespaces;

    @ReadOnlyProperty
    private int id;

    @ReadOnlyProperty
    @JsonProperty("api_version")
    private String apiVersion;

    @JsonProperty("created_at")
    private DateTime createdAt;

    @JsonProperty("updated_at")
    private DateTime updatedAt;



    /**
     * @param name
     * @param value
     */
    @JsonAnySetter
    public void ignored(String name, Object value) {
        log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
    }
}
