package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@ActiveProfiles("integration-test")
@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSummaryControllerSubmittedTest extends AbstractControllerTest {

    private static final long CASE_ID = 1243L;

    CaseSummaryControllerSubmittedTest() {
        super("case-summary");
    }

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldUpdateTaskList() {
        when(featureToggleService.isSummaryTabOnEventEnabled()).thenReturn(true);

        postSubmittedEvent(CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of(
                "solicitor", Map.of(
                    "name", "John Smith"
                )
            )).build());

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), ArgumentMatchers.eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());
        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldUpdateTaskListToggledOff() {
        when(featureToggleService.isSummaryTabOnEventEnabled()).thenReturn(false);

        postSubmittedEvent(CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of(
                "solicitor", Map.of(
                    "name", "John Smith"
                )
            )).build());

        verifyNoInteractions(coreCaseDataService);
    }
}
