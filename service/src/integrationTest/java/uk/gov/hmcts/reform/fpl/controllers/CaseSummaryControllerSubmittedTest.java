package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSummaryControllerSubmittedTest extends AbstractCallbackTest {

    private static final long CASE_ID = 1243L;

    CaseSummaryControllerSubmittedTest() {
        super("case-summary");
    }

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    void shouldUpdateTaskList() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .solicitor(Solicitor.builder()
                .name("John Smith")
                .build())
            .build();

        postSubmittedEvent(caseData);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());
        verifyNoMoreInteractions(coreCaseDataService);
    }
}
