package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.event.AllocateJudgeEventData;
import uk.gov.hmcts.reform.fpl.service.ElinksService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.rd.client.JudicialApi;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(AllocatedJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
@Import({JudicialService.class})
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

    @MockBean
    private SystemUserService systemUserService;

    @MockBean
    private RoleAssignmentService roleAssignmentService;

    @MockBean
    private ValidateEmailService validateEmailService;

    @MockBean
    private ElinksService elinksService;

    @WithMockUser
    @Test
    void shouldAddExtraInfoIfInMapping() {
        when(legalAdviserUsersConfiguration.getLegalAdviserUUID("test@test.com"))
            .thenReturn(Optional.of("1234"));

        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.LEGAL_ADVISOR, null, null,
                Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                    .judgeEmailAddress("test@test.com")
                    .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData after = extractCaseData(callbackResponse);

        assertThat((callbackResponse.getErrors())).isNull();
        assertThat(after.getAllocatedJudge().getJudgeJudicialUser().getIdamId()).isEqualTo("1234");
    }

    @WithMockUser
    @Test
    void shouldErrorIfNoJudgeAndNotEnteringManually() {
        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.SALARIED_JUDGE, null, null, Judge.builder().build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getErrors())
            .containsExactly("You must search for a judge or enter their details manually");
    }

    @WithMockUser
    @Test
    void shouldErrorIfNoJudgeFoundInJrd() {
        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.SALARIED_JUDGE, null, JudicialUser.builder().personalCode("1234").build(), null))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getErrors())
            .containsExactly("Judge could not be found, please search again or enter their details manually");
    }

    @WithMockUser
    @Test
    void shouldClearLastNameIfMagistrates() {
        Judge judge = Judge.builder()
            .judgeEmailAddress("email@example.com")
            .judgeLastName("lastName")
            .judgeFullName("fullName")
            .judgeTitle(JudgeOrMagistrateTitle.MAGISTRATES)
            .build();
        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.LEGAL_ADVISOR, null, null, judge))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        CaseData caseDataResult = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseDataResult.getAllocatedJudge())
            .isEqualTo(judge.toBuilder().judgeType(JudgeType.LEGAL_ADVISOR).judgeLastName(null).build());
    }

    @WithMockUser
    @Test
    void shouldClearFullNameIfNotMagistrates() {
        Judge judge = Judge.builder()
            .judgeEmailAddress("email@example.com")
            .judgeLastName("lastName")
            .judgeFullName("fullName")
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .build();
        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.LEGAL_ADVISOR, null, null, judge))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        CaseData caseDataResult = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseDataResult.getAllocatedJudge())
            .isEqualTo(judge.toBuilder().judgeType(JudgeType.LEGAL_ADVISOR).judgeFullName(null).build());
    }
}
