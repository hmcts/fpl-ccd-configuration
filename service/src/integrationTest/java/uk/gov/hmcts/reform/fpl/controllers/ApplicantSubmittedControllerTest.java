package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;

@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantSubmittedControllerTest extends AbstractCallbackTest {

    private static final long CASE_ID = 12323L;
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;

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

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).performPostSubmitCallback(eq(CASE_ID),
            eq("internal-update-task-list"), any());

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).performPostSubmitCallback(eq(CASE_ID),
            eq("internal-update-case-summary"), any());

    }
}
