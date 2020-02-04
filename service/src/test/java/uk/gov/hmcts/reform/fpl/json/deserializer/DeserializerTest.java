package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class DeserializerTest {
    protected ObjectMapper mapper;

    protected  <T> DeserializerTest(Class<T> type, JsonDeserializer<? extends T> deserializer) {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(type, deserializer);
        mapper.registerModule(module);
    }
}
