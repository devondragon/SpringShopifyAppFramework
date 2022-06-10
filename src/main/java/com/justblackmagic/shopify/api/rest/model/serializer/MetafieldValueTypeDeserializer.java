package com.justblackmagic.shopify.api.rest.model.serializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.justblackmagic.shopify.api.rest.model.MetafieldValueType;

public class MetafieldValueTypeDeserializer extends StdDeserializer<MetafieldValueType> {

    public MetafieldValueTypeDeserializer() {
        this(MetafieldValueType.class);
    }

    protected MetafieldValueTypeDeserializer(Class<MetafieldValueType> t) {
        super(t);
    }


    /**
     * @param p
     * @param ctxt
     * @return MetafieldValueType
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public MetafieldValueType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return MetafieldValueType.toEnum(p.getText());
    }

}
