package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseAssignmentService;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(NoticeOfChangeController.class)
@OverrideAutoConfiguration(enabled = true)
public class NoticeOfChangeControllerUpdateRespondentsTest extends AbstractCallbackTest {

    @MockBean
    private CaseAssignmentService caseAssignmentService;

    @TestLogs
    private TestLogger logs = new TestLogger(NoticeOfChangeController.class);

    NoticeOfChangeControllerUpdateRespondentsTest() {
        super("noc-decision/update-respondents");
    }

    @Test
    void shouldInvokeAACApi() {
        CaseData caseData = CaseData.builder().build();
        when(caseAssignmentService.applyDecision(any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of())
            .build());

        postAboutToStartEvent(caseData);

        verify(caseAssignmentService).applyDecision(any());
    }

    @Test
    void shouldLogWhenAACFailure() {
        CaseData caseData = CaseData.builder().build();
        when(caseAssignmentService.applyDecision(any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("Some error from AAC"))
            .build());

        postAboutToStartEvent(caseData);

        verify(caseAssignmentService).applyDecision(any());
        assertThat(logs.getErrors()).contains("Some error from AAC");

    }

    @Test
    void shouldClearOnComplete() {
        CaseData caseData = CaseData.builder()
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse resp = postAboutToSubmitEvent(caseData);

        assertThat(resp.getData()).doesNotContainKey("changeOrganisationRequestField");
    }
}
