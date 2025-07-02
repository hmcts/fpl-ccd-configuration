package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(CaseSummaryController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSummaryControllerSubmittedTest extends AbstractCallbackTest {

    private static final long CASE_ID = 1243L;

    CaseSummaryControllerSubmittedTest() {
        super("case-summary");
    }

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    void shouldUpdateCaseSummaryWhenRelevantFieldChanged() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .designated("Yes")
                .colleagues(wrapElements(Colleague.builder()
                    .role(ColleagueRole.SOLICITOR)
                    .firstName("John")
                    .lastName("Smith")
                    .mainContact("Yes")
                    .build()))
                .build()))
            .build();

        postSubmittedEvent(caseData);

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID),
            eq("internal-update-case-summary"), any());
        verifyNoMoreInteractions(coreCaseDataService);
    }
}
