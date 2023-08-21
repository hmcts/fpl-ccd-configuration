package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.rd.client.JudicialApi;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(AllocatedJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocatedJudgeControllerAboutToSubmitTest extends AbstractCallbackTest {

    AllocatedJudgeControllerAboutToSubmitTest() {
        super("allocated-judge");
    }

    @MockBean
    private JudicialApi jrdApi;

    @MockBean
    private JudicialUsersConfiguration judicialUsersConfiguration;

    @MockBean
    private LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;

    @Test
    void shouldAddExtraInfoIfInMapping() {
        when(legalAdviserUsersConfiguration.getLegalAdviserUUID("test@test.com"))
            .thenReturn(Optional.of("1234"));

        CaseData caseData = CaseData.builder()
            .enterManually(YesNo.YES)
            .allocatedJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                .judgeEmailAddress("test@test.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData after = extractCaseData(callbackResponse);

        assertThat((callbackResponse.getErrors())).isNull();
        assertThat(after.getAllocatedJudge().getJudgeJudicialUser().getIdamId()).isEqualTo("1234");
    }

    @Test
    void shouldErrorIfNoJudgeAndNotEnteringManually() {
        CaseData caseData = CaseData.builder()
            .enterManually(YesNo.NO)
            .allocatedJudge(Judge.builder().build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getErrors())
            .containsExactly("You must search for a judge or enter their details manually");
    }

    @Test
    void shouldErrorIfNoJudgeFoundInJrd() {
        CaseData caseData = CaseData.builder()
            .enterManually(YesNo.NO)
            .judicialUser(JudicialUser.builder().personalCode("1234").build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getErrors())
            .containsExactly("Could not fetch Judge details from JRD, please try again in a few minutes.");
    }

}
