package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicantSubmittedControllerTest extends AbstractCallbackTest {

    private static final long CASE_ID = 12323L;

    @MockBean
    private PbaNumberService pbaNumberService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private ValidateEmailService validateEmailService;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private RequestData requestData;

    @MockBean
    private SystemUserService systemUserService;

    @MockBean
    private CCDConcurrencyHelper concurrencyHelper;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ApplicantSubmittedControllerTest() {
        super("enter-applicant");
    }

    @WithMockUser
    @Test
    void shouldUpdateTaskListAndSummary() throws Exception {
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
