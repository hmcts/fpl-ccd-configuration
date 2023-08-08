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
import uk.gov.hmcts.reform.rd.client.JudicialApi;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(AllocatedJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocatedJudgeControllerMidEventTest extends AbstractCallbackTest {

    AllocatedJudgeControllerMidEventTest() {
        super("allocated-judge");
    }

    @MockBean
    private JudicialApi jrdApi;

    @Test
    void shouldNotReturnAValidationErrorWhenJudgePersonalCodeAdded() {
        given(jrdApi.findUsers(any(), any(), any(), any())).willReturn(List.of(JudicialUserProfile.builder()
            .build()));
        CaseData caseData = CaseData.builder()
            .enterManually(YesNo.NO)
            .judicialUser(JudicialUser.builder()
                .personalCode("1234")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat((callbackResponse.getErrors())).isNull();
    }

    @Test
    void shouldNotReturnAValidationErrorWhenJudgeEnteredManually() {
        CaseData caseData = CaseData.builder()
            .enterManually(YesNo.YES)
            .allocatedJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                .judgeEmailAddress("email@example.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat((callbackResponse.getErrors())).isNull();
    }

    @Test
    void shouldReturnAValidationErrorWhenNoPersonalCode() {
        CaseData caseData = CaseData.builder()
            .enterManually(YesNo.NO)
            .judicialUser(JudicialUser.builder()
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(
            "You must search for a judge or enter their details manually");
    }

    @Test
    void shouldReturnAValidationErrorWhenEnterManuallyAndInvalidEmail() {
        CaseData caseData = CaseData.builder()
            .enterManually(YesNo.YES)
            .allocatedJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                .judgeEmailAddress("<not valid email address>")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(
            "Enter an email address in the correct format, for example name@example.com");
    }

}
