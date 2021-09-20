package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;

@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantSubmittedControllerTest extends AbstractCallbackTest {

    private static final long CASE_ID = 12323L;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ApplicantSubmittedControllerTest() {
        super("enter-applicant");
    }

    @Test
    void shouldUpdateTaskListAndSummary() {

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .state(State.OPEN)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .solicitor(Solicitor.builder()
                .name("John Smith")
                .build())
            .build();

        postSubmittedEvent(caseData);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-task-list"), anyMap());

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());

    }
}
