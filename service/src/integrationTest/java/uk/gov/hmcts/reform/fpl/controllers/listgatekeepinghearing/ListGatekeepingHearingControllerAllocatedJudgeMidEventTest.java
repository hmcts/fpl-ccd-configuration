package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ListGatekeepingHearingController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;

@WebMvcTest(ListGatekeepingHearingController.class)
@OverrideAutoConfiguration(enabled = true)
class ListGatekeepingHearingControllerAllocatedJudgeMidEventTest extends AbstractCallbackTest {

    ListGatekeepingHearingControllerAllocatedJudgeMidEventTest() {
        super("list-gatekeeping-hearing/allocated-judge");
    }

    @Test
    void shouldNotReturnAValidationErrorWhenJudgeEmailIsValidAndSetJudgeAndLegalAdvisorField() {

        final CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeTitle(DISTRICT_JUDGE)
                .judgeLastName("Judge")
                .judgeEmailAddress("email@example.com")
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        final CaseData responseData = extractCaseData(callbackResponse);

        assertThat((callbackResponse.getErrors())).isNull();
        assertThat(responseData.getJudgeAndLegalAdvisor().getAllocatedJudgeLabel())
            .isEqualTo("Case assigned to: District Judge Judge");
    }

    @Test
    void shouldReturnAValidationErrorWhenJudgeEmailIsInvalid() {

        final CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("<John Doe> johndoe@email.com")
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(
            "Enter an email address in the correct format, for example name@example.com");
    }
}
