package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
class ManageHearingsControllerValidateJudgeEmailMidEventTest extends ManageHearingsControllerTest {

    @MockBean
    private JudicialApi jrdApi;

    ManageHearingsControllerValidateJudgeEmailMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldNotReturnAValidationErrorWhenJudgeEmailIsValid() {
        CaseData caseData = CaseData.builder()
            .useAllocatedJudge(YesNo.NO)
            .enterManuallyHearingJudge(YesNo.YES)
            .hearingJudge(Judge.builder()
                .judgeEmailAddress("email@example.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-judge-email");

        assertThat(callbackResponse.getErrors()).isNull();
    }

    @Test
    void shouldNotReturnAValidationErrorWhenJudgePersonalCodeAdded() {
        given(jrdApi.findUsers(any(), any(), any(), any())).willReturn(List.of(JudicialUserProfile.builder()
            .build()));
        CaseData caseData = CaseData.builder()
            .useAllocatedJudge(YesNo.NO)
            .enterManuallyHearingJudge(YesNo.NO)
            .judicialUserHearingJudge(JudicialUser.builder()
                .personalCode("1234")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-judge-email");

        assertThat((callbackResponse.getErrors())).isNull();
    }

    @Test
    void shouldReturnAValidationErrorWhenNoJudgeSearchedAndNotEnteredManually() {
        CaseData caseData = CaseData.builder()
            .useAllocatedJudge(YesNo.NO)
            .enterManuallyHearingJudge(YesNo.NO)
            .judicialUserHearingJudge(JudicialUser.builder().build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-judge-email");

        assertThat((callbackResponse.getErrors())).containsExactly(
            "You must search for a judge or enter their details manually");
    }

    @Test
    void shouldReturnAValidationErrorWhenJudgeEmailIsInvalid() {
        CaseData caseData = CaseData.builder()
            .enterManuallyHearingJudge(YesNo.YES)
            .useAllocatedJudge(YesNo.NO)
            .hearingJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeEmailAddress("<John Doe> johndoe@email.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-judge-email");

        assertThat(callbackResponse.getErrors()).containsExactly(
            "Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldNotReturnAValidationErrorWhenAllocatedJudgeIsUsed() {
        CaseData caseData = CaseData.builder()
            .enterManuallyHearingJudge(YesNo.YES)
            .useAllocatedJudge(YesNo.YES)
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("email@email.com")
                .build())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "validate-judge-email");

        assertThat(callbackResponse.getErrors()).isNull();
    }
}
