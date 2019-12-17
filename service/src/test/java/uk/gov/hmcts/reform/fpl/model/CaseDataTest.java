package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.JUDGE_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class CaseDataTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldSerialiseCaseManagementOrderToCorrectStringValueWhenInSelfReview() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SELF_REVIEW).build())
            .build());

        JSONAssert.assertEquals("{caseManagementOrder:{status: SELF_REVIEW}}", serialised, false);
    }

    @Test
    void shouldSerialiseCaseManagementOrderToCorrectStringValueWhenInSendToJudge() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(JUDGE_REVIEW).build())
            .build());

        JSONAssert.assertEquals("{cmoToAction:{status: JUDGE_REVIEW}}", serialised, false);
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithSelfReviewState() throws JsonProcessingException {
        String content = "{\"caseManagementOrder\":{\"status\": \"SELF_REVIEW\"}}";

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SELF_REVIEW).build())
            .build());
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithSendToJudgeState() throws JsonProcessingException {
        String content = "{\"cmoToAction\":{\"status\": \"JUDGE_REVIEW\"}}";

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(JUDGE_REVIEW).build())
            .build());
    }
}
