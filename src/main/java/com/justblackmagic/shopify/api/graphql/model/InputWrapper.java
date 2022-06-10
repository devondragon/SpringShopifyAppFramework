package com.justblackmagic.shopify.api.graphql.model;

import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class InputWrapper {

    private Map<String, String> input;

    public InputWrapper(Map<String, String> input) {
        this.input = input;
    }

    public InputWrapper() {}

    @SuppressWarnings("unchecked")
    public InputWrapper(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.input = mapper.readValue(jsonString, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing json string", e);
        }
    }

}
