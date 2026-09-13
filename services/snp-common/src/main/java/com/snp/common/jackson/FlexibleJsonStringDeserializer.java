package com.snp.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Accepts either a JSON string or a JSON object/array for String fields (e.g. contentJson).
 */
public class FlexibleJsonStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_STRING) {
            String text = p.getValueAsString();
            return text == null || text.isBlank() ? null : text;
        }
        JsonNode node = p.readValueAsTree();
        return node == null || node.isNull() ? null : node.toString();
    }
}
