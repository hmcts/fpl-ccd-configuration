package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(AllocatedJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocatedJudgeControllerMidEventTest extends AbstractCallbackTest {
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

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat((callbackResponse.getErrors())).isNull();
    }

    @Test
    void shouldReturnAValidationErrorWhenJudgeEmailIsInvalid() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("<John Doe> johndoe@email.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(
            "Enter an email address in the correct format, for example name@example.com");
    }


    @Test
    void shouldClearLastNameIfMagistrates() {
        Judge judge = Judge.builder()
            .judgeEmailAddress("email@example.com")
            .judgeLastName("lastName")
            .judgeFullName("fullName")
            .judgeTitle(JudgeOrMagistrateTitle.MAGISTRATES)
            .build();
        CaseData caseData = CaseData.builder()
            .allocatedJudge(judge)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        CaseData caseDataResult = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseDataResult.getAllocatedJudge())
            .isEqualTo(judge.toBuilder().judgeLastName(null).build());
    }

    @Test
    void shouldClearFullNameIfNotMagistrates() {
        Judge judge = Judge.builder()
            .judgeEmailAddress("email@example.com")
            .judgeLastName("lastName")
            .judgeFullName("fullName")
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .build();
        CaseData caseData = CaseData.builder()
            .allocatedJudge(judge)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        CaseData caseDataResult = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseDataResult.getAllocatedJudge())
            .isEqualTo(judge.toBuilder().judgeFullName(null).build());
    }
}
