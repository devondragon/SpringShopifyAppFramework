package com.justblackmagic.shopify.api.rest.model.serializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.justblackmagic.shopify.api.rest.model.MetafieldValueType;

public class MetafieldValueTypeSerializer extends StdSerializer<MetafieldValueType> {

    public MetafieldValueTypeSerializer() {
        this(MetafieldValueType.class);
    }

    protected MetafieldValueTypeSerializer(Class<MetafieldValueType> t) {
        super(t);
    }


    /**
     * @param value
     * @param gen
     * @param provider
     * @throws IOException
     */
    @Override
    public void serialize(MetafieldValueType value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());

    }

}
