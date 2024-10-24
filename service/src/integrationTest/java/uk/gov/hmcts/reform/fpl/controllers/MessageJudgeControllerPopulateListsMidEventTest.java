package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerPopulateListsMidEventTest extends AbstractCallbackTest {
    private static final UUID DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();

    MessageJudgeControllerPopulateListsMidEventTest() {
        super("message-judge/populate-related-docs");
    }

    @Test
    void shouldSetHearingLabelWhenNextHearingExists() {
        HearingBooking expectedNextHearing = HearingBooking.builder()
            .startDate(now().plusDays(1))
            .type(CASE_MANAGEMENT)
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .startDate(now().plusDays(3))
                    .type(FINAL)
                    .build()),
                element(expectedNextHearing),
                element(HearingBooking.builder()
                    .startDate(now().plusDays(5))
                    .type(ISSUE_RESOLUTION)
                    .build())
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData().get("nextHearingLabel")).isEqualTo(
            String.format("Next hearing in the case: %s", expectedNextHearing.toLabel()));
    }

    @Test
    void shouldNotReturnAValidationErrorWhenEmailIsValid() {
        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData
                .builder()
                .judicialMessageMetaData(JudicialMessageMetaData
                    .builder()
                    .recipient("valid-email@test.com")
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnAValidationErrorWhenEmailIsInvalid() {
        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData
                .builder()
                .judicialMessageMetaData(JudicialMessageMetaData
                    .builder()
                    .recipient("Test user <Test.User@HMCTS.NET>")
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getErrors()).contains(
            "Enter an email address in the correct format, for example name@example.com");
    }
}
