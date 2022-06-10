package com.justblackmagic.shopify.api.rest.model.serializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.justblackmagic.shopify.api.rest.model.InventoryPolicy;

public class InventoryPolicySerializer extends StdSerializer<InventoryPolicy> {

    public InventoryPolicySerializer() {
        this(InventoryPolicy.class);
    }

    protected InventoryPolicySerializer(Class<InventoryPolicy> t) {
        super(t);
    }


    /**
     * @param value
     * @param gen
     * @param provider
     * @throws IOException
     */
    @Override
    public void serialize(InventoryPolicy value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());

    }

}
