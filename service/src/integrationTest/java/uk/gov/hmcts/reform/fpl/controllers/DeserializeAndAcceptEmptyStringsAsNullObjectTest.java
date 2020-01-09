package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("integration-test")
@WebMvcTest(DeserializeAndAcceptEmptyStringsAsNullObjectTest.class)
@OverrideAutoConfiguration(enabled = true)
class DeserializeAndAcceptEmptyStringsAsNullObjectTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldDeserializeAndAcceptEmptyStringsAsNullObject() throws IOException {
        CaseData data = mapper.readValue(ResourceReader
            .readString("fixtures/caseDataWithEmptyStringsInsteadOfNull.json"), CaseData.class);

        Element<Direction> expectedCourtDirections = buildExpectedCourtDirections();

        assertEquals(data.getCourtDirections().get(0), expectedCourtDirections);
    }

    private Element<Direction> buildExpectedCourtDirections() {
        return Element.<Direction>builder()
            .id(UUID.fromString("1814cb97-d87b-4968-bf47-11ad0f4a5607"))
            .value(
                Direction.builder()
                    .directionType("Arrange interpreters")
                    .directionText(null)
                    .status(null)
                    .assignee(DirectionAssignee.COURT)
                    .parentsAndRespondentsAssignee(null)
                    .otherPartiesAssignee(null)
                    .readOnly("Yes")
                    .directionRemovable("Yes")
                    .directionNeeded(null)
                    .custom(null)
                    .dateToBeCompletedBy(null)
                    .response(DirectionResponse.builder()
                        .directionId(null)
                        .assignee(null)
                        .complied(null)
                        .documentDetails(null)
                        .file(null)
                        .cannotComplyFile(null)
                        .cannotComplyReason(null)
                        .c2Uploaded(null)
                        .respondingOnBehalfOf("")
                        .build())
                    .responses(null)
                    .build())
            .build();
    }
}
