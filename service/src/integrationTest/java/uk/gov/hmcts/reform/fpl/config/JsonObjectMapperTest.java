package uk.gov.hmcts.reform.fpl.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.controllers.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class JsonObjectMapperTest extends AbstractTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldDisableAutoCompleteJsonFeature() {
        assertFalse(objectMapper.getFactory().isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT));
    }
}
