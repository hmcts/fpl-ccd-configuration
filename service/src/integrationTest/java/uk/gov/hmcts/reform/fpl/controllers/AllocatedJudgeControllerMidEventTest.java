package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(AllocationDecisionController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocatedJudgeControllerMidEventTest extends AbstractControllerTest {
    AllocatedJudgeControllerMidEventTest() {
        super("allocated-judge");
    }

    @Test
    void shouldNotReturnAValidationErrorWhenJudgeEmailIsValid() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("email@example.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat((callbackResponse.getErrors())).isNull();
    }

    @Test
    void shouldReturnAValidationErrorWhenJudgeEmailIsInvalid() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("<John Doe> johndoe@email.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).contains(
            "Enter an email address in the correct format, for example name@example.com");
    }
}
