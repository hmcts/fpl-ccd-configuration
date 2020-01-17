package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;

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

        JSONAssert.assertEquals(String.format("{%s:{status: %s}}",
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), SELF_REVIEW.name()), serialised, false);
    }

    @Test
    void shouldSerialiseCaseManagementOrderToCorrectStringValueWhenInSendToJudge() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build())
            .build());

        JSONAssert.assertEquals(
            String.format("{%s:{status: %s}}", CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), SEND_TO_JUDGE.name()),
            serialised, false);
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithSelfReviewState() throws JsonProcessingException {
        String content = String.format("{\"%s\":{\"status\": \"%s\"}}",
            CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), SELF_REVIEW.name());

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SELF_REVIEW).build())
            .build());
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithSendToJudgeState() throws JsonProcessingException {
        String content = String.format("{\"%s\":{\"status\": \"%s\"}}",
            CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), SEND_TO_JUDGE.name());

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().status(SEND_TO_JUDGE).build())
            .build());
    }

    @Test
    public void shouldGetAllOthers() {
        Other other1 = Other.builder().build();
        Other other2 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .additionalOthers(ElementUtils.wrapElements(other2))
                .build())
            .build();

        assertThat(caseData.getAllOthers().get(0).getValue()).isEqualTo(other1);
        assertThat(caseData.getAllOthers().get(1).getValue()).isEqualTo(other2);
    }

    @Test
    public void shouldGetEmptyListOfOthersWhenOthersIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertThat(caseData.getAllOthers()).isEmpty();
    }

    @Test
    public void shouldGetEmptyListOfOthersWhenOthersAreEmpty() {
        CaseData caseData = CaseData.builder()
            .others(Others.builder().build())
            .build();

        assertThat(caseData.getAllOthers().equals(null));
    }

    @Test
    public void shouldGetFirstOtherWhenNoAdditionalOthers() {
        Other other1 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .build())
            .build();

        assertThat(caseData.getAllOthers().get(0).getValue()).isEqualTo(other1);
    }

    @Test
    public void shouldFindFirstOther() {
        Other other1 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .build())
            .build();
        assertThat(caseData.findOther(0)).isEqualTo(Optional.of(other1));
    }

    @Test
    public void shouldNotFindNonExistingOther() {
        Other other1 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .build())
            .build();
        assertThat(caseData.findOther(1)).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldFindExistingOther() {
        Other other1 = Other.builder().build();
        Other other2 = Other.builder().build();
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(other1)
                .additionalOthers(ElementUtils.wrapElements(other2))
                .build())
            .build();
        assertThat(caseData.findOther(1)).isEqualTo(Optional.of(other2));
    }

    @Test
    public void shouldFindExistingRespondent() {
        Respondent respondent = Respondent.builder().build();
        CaseData caseData = CaseData.builder()
            .respondents1(ElementUtils.wrapElements(respondent))
            .build();

        assertThat(caseData.findRespondent(0)).isEqualTo(Optional.of(respondent));
    }

    @Test
    public void shouldNotFindNonExistingRespondent() {
        Respondent respondent = Respondent.builder().build();
        CaseData caseData = CaseData.builder()
            .respondents1(ElementUtils.wrapElements(respondent))
            .build();

        assertThat(caseData.findRespondent(1)).isEqualTo(Optional.empty());
    }

    @Nested
    class GetFurtherDirectionsText {
        private FurtherDirections furtherDirections;
        private CaseData caseData;

        @Test
        void shouldReturnDirectionTextWhenFurtherDirectionIsPopulated() {
            furtherDirections = FurtherDirections.builder().directions("some text").build();
            caseData = CaseData.builder().orderFurtherDirections(furtherDirections).build();

            assertThat(caseData.getFurtherDirectionsText()).isEqualTo("some text");
        }

        @Test
        void shouldReturnEmptyStringWhenFurtherDirectionIsNotPopulated() {
            furtherDirections = FurtherDirections.builder().build();
            caseData = CaseData.builder().orderFurtherDirections(furtherDirections).build();

            assertThat(caseData.getFurtherDirectionsText()).isEmpty();
        }

        @Test
        void shouldReturnEmptyStringWhenFurtherDirectionIsNull() {
            caseData = CaseData.builder().build();

            assertThat(caseData.getFurtherDirectionsText()).isEmpty();
        }
    }
}
