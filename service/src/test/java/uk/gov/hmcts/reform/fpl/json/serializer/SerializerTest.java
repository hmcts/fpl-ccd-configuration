package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class SerializerTest {
    protected ObjectMapper mapper;

    protected <T> SerializerTest(Class<? extends T> type, JsonSerializer<T> serializer) {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(type, serializer);
        mapper.registerModule(module);
    }
}
