package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ListGatekeepingHearingController;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;

@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ListGatekeepingHearingController.class)
class ListGatekeepingHearingControllerValidateJudgeEmailMidEventTest extends AbstractCallbackTest {

    ListGatekeepingHearingControllerValidateJudgeEmailMidEventTest() {
        super("list-gatekeeping-hearing");
    }

    //@Test
    void shouldNotReturnAValidationErrorWhenJudgeEmailIsValid() {

        final CaseData caseData = CaseData.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEmailAddress("email@example.com")
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-judge-email");

        assertThat(callbackResponse.getErrors()).isNull();
    }

    //@Test
    void shouldReturnAValidationErrorWhenJudgeEmailIsInvalid() {

        final CaseData caseData = CaseData.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeEmailAddress("<John Doe> johndoe@email.com")
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-judge-email");

        assertThat(callbackResponse.getErrors()).containsExactly(
            "Enter an email address in the correct format, for example name@example.com");
    }

    //@Test
    void shouldNotReturnAValidationErrorWhenAllocatedJudgeIsUsed() {

        final CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("email@email.com")
                .build())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-judge-email");

        assertThat(callbackResponse.getErrors()).isNull();
    }
}
