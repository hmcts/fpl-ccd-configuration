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
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@WebMvcTest(AllocatedJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
@Import({JudicialService.class, ValidateEmailService.class})
class AllocatedJudgeControllerMidEventTest extends AbstractCallbackTest {

    AllocatedJudgeControllerMidEventTest() {
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
    private ElinksService elinksService;


    @WithMockUser
    @Test
    void shouldNotReturnAValidationErrorWhenJudgePersonalCodeAdded() {
        given(jrdApi.findUsers(any(), any(), anyInt(), any(), any())).willReturn(List.of(JudicialUserProfile.builder()
            .build()));
        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.SALARIED_JUDGE, null,
                JudicialUser.builder().personalCode("1234").build(), null
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat((callbackResponse.getErrors())).isNull();
    }

    @WithMockUser
    @Test
    void shouldNotReturnAValidationErrorWhenJudgeEnteredManually() {
        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.LEGAL_ADVISOR, null, null,
                Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                    .judgeEmailAddress("email@example.com")
                    .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat((callbackResponse.getErrors())).isNull();
    }

    @WithMockUser
    @Test
    void shouldReturnAValidationErrorWhenNoPersonalCode() {
        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.SALARIED_JUDGE, null, JudicialUser.builder().build(), null))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(
            "You must search for a judge or enter their details manually");
    }

    @WithMockUser
    @Test
    void shouldReturnAValidationErrorWhenEnterManuallyAndInvalidEmail() {
        CaseData caseData = CaseData.builder()
            .allocateJudgeEventData(new AllocateJudgeEventData(
                JudgeType.LEGAL_ADVISOR, null, null,
                Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                    .judgeEmailAddress("<not valid email address>")
                    .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(
            "Enter an email address in the correct format, for example name@example.com");
    }
}
