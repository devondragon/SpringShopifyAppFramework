package com.justblackmagic.shopify.api.rest.model.serializer;

import java.io.IOException;
import java.util.Collection;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.text.StringEscapeUtils;

public class EscapedStringsSerializer extends StdSerializer<Collection<String>> {

    public EscapedStringsSerializer() {
        this(null);
    }

    protected EscapedStringsSerializer(Class<Collection<String>> t) {
        super(t);
    }


    /**
     * @param value
     * @param jgen
     * @param provider
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public void serialize(Collection<String> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeString((value == null) ? null : value.stream().map(StringEscapeUtils::unescapeHtml4).toString());
    }
}

