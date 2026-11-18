
package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.exceptions.api.AuthorizationException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.legalcounsel.ManageLegalCounselService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.anyLong;

@WebMvcTest(ManageApplicantLegalCounselController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageApplicantLegalCounselControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private ManageLegalCounselService manageLegalCounselService;

    ManageApplicantLegalCounselControllerAboutToStartTest() {
        super("manage-applicant-legal-counsel");
    }

    @Test
    void shouldRejectUnauthorisedRespondentSolicitor() {
        CaseData caseData = CaseData.builder()
            .id(1234567890123456L)
            .build();

        doThrow(new AuthorizationException(
            "Current user is not authorised to manage Applicant legal counsel"
        )).when(manageLegalCounselService)
            .getAuthorisedApplicantCounselCaseData(anyLong());

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).containsExactly(
            "You must be the Applicant or acting on behalf of the Applicant to manage Applicant legal counsel."
        );

        verify(manageLegalCounselService, never())
            .retrieveApplicantLegalCounselForCurrentUser(any(CaseData.class));

        assertThat(response.getData()).containsEntry("legalCounsellors", null);
    }

    @Test
    void shouldAllowAuthorisedApplicantSolicitor() {
        CaseData caseData = CaseData.builder()
            .id(1234567890123456L)
            .build();

        when(manageLegalCounselService.getAuthorisedApplicantCounselCaseData(anyLong()))
            .thenReturn(caseData);

        when(manageLegalCounselService.retrieveApplicantLegalCounselForCurrentUser(any(CaseData.class)))
            .thenReturn(List.of());

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).isNullOrEmpty();

        verify(manageLegalCounselService)
            .retrieveApplicantLegalCounselForCurrentUser(caseData);

        assertThat(response.getData()).containsEntry("legalCounsellors", List.of());
    }
}
