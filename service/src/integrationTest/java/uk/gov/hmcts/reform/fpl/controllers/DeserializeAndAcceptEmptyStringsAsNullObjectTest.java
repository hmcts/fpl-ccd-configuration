package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.io.File;
import java.io.IOException;

@ActiveProfiles("integration-test")
@WebMvcTest(DeserializeAndAcceptEmptyStringsAsNullObjectTest.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class DeserializeAndAcceptEmptyStringsAsNullObjectTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldDeserializeAndAcceptEmptyStringsAsNullObject() throws IOException {
        try {
            mapper.readValue(new
                File("src/integrationTest/resources/fixtures/caseDataWithEmptyStringsInsteadOfNull.json"),
                CaseData.class);
        } catch (IOException ex) {
            throw new IOException(ex);
        }
    }
}
