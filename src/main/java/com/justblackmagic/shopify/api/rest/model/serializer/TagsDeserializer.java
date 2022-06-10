package com.justblackmagic.shopify.api.rest.model.serializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;

public class TagsDeserializer extends StdDeserializer<Set<String>> {


    public TagsDeserializer() {
        this(null);
    }

    protected TagsDeserializer(Class<Set<String>> t) {
        super(t);
    }


    /**
     * @param p
     * @param ctxt
     * @return Set<String>
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public Set<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String tags = p.getText();
        if (StringUtils.isBlank(tags)) {
            return Collections.emptySet();
        }
        final String[] tagArray = tags.split(TagsSerializer.TAG_SERIALIZATION_DELIMITTER);
        return new HashSet<>(Arrays.asList(tagArray));
    }

}
