package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.util.Base64;

public class Base64Deserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private Class<?> resultClass;

    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        Base64.Decoder decoder = Base64.getDecoder();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] decodedValue = decoder.decode(value);
            return objectMapper.readValue(decodedValue, this.resultClass);
        } catch (IllegalArgumentException | JsonParseException e) {
            String fieldName = parser.getParsingContext().getCurrentName();
            Class<?> wrapperClass = parser.getParsingContext().getCurrentValue().getClass();

            throw new InvalidFormatException(
                parser,
                String.format("Value for '%s' is not a base64 encoded JSON", fieldName),
                value,
                wrapperClass
            );
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        this.resultClass = property.getType().getRawClass();
        return this;
    }
}
