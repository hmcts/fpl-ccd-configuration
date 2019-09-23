package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.IOException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JacksonAutoConfiguration.class)
class JsonOrdersLookupServiceTest {

    private final JsonOrdersLookupService jsonOrdersLookupService;

    @Autowired
    public JsonOrdersLookupServiceTest(ObjectMapper objectMapper) {
        this.jsonOrdersLookupService = new JsonOrdersLookupService(objectMapper);
    }

    @Test
    public void test() throws IOException {
        OrderDefinition standardDirectionOrder = jsonOrdersLookupService.getStandardDirectionOrder();

        System.out.println(standardDirectionOrder);
    }

}
