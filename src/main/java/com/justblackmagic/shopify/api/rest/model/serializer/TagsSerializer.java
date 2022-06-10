package com.justblackmagic.shopify.api.rest.model.serializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TagsSerializer extends StdSerializer<Set<String>> {


    public static final String TAG_SERIALIZATION_DELIMITTER = ", ";

    public TagsSerializer() {
        this(null);
    }

    protected TagsSerializer(Class<Set<String>> t) {
        super(t);
    }


    /**
     * @param value
     * @param gen
     * @param provider
     * @throws IOException
     */
    @Override
    public void serialize(Set<String> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if ((value == null) || value.isEmpty()) {
            gen.writeNull();
        }

        final StringBuilder tagStringBuilder = new StringBuilder();
        final Iterator<String> tagIterator = value.iterator();
        while (tagIterator.hasNext()) {
            final String tag = tagIterator.next();
            tagStringBuilder.append(tag);
            if (tagIterator.hasNext()) {
                tagStringBuilder.append(TAG_SERIALIZATION_DELIMITTER);
            }
        }
        gen.writeFieldName("tags");
        gen.writeString(tagStringBuilder.toString());
    }

}
