package com.justblackmagic.shopify.api.rest.model.serializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.justblackmagic.shopify.api.rest.model.OrderRiskRecommendation;

public class OrderRiskRecommendationDeserializer extends StdDeserializer<OrderRiskRecommendation> {

    public OrderRiskRecommendationDeserializer() {
        this(OrderRiskRecommendation.class);
    }

    protected OrderRiskRecommendationDeserializer(Class<OrderRiskRecommendation> t) {
        super(t);
    }


    /**
     * @param p
     * @param ctxt
     * @return OrderRiskRecommendation
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public OrderRiskRecommendation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return OrderRiskRecommendation.toEnum(p.getText());
    }

}
