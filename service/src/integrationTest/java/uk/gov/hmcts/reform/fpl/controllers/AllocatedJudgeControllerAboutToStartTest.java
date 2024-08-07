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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ElinksService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.rd.client.JudicialApi;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(AllocatedJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
@Import({JudicialService.class})
class AllocatedJudgeControllerAboutToStartTest extends AbstractCallbackTest {

    AllocatedJudgeControllerAboutToStartTest() {
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
    void shouldSetEnterManuallyDefaultToNo() {
        CaseData caseData = CaseData.builder()
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);
        CaseData after = extractCaseData(callbackResponse);

        assertThat(after.getEnterManually()).isEqualTo(YesNo.NO);
    }

}
