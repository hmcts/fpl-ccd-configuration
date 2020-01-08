package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles("integration-test")
@WebMvcTest(DeserializeAndAcceptEmptyStringsAsNullObjectTest.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class DeserializeAndAcceptEmptyStringsAsNullObjectTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldDeserializeAndAcceptEmptyStringsAsNullObject() throws IOException {
        CaseData data = mapper.readValue(ResourceReader.readString("fixtures/caseDataWithEmptyStringsInsteadOfNull.json"), CaseData.class);

        ParentsAndRespondentsDirectionAssignee parentsAndRespondentsDirectionAssignee = data.getCourtDirections().get(0).getValue().getParentsAndRespondentsAssignee();

        assertNull(parentsAndRespondentsDirectionAssignee);


    }
}
