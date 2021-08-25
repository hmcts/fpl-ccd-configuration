package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryCourtGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseFlagControllerSubmittedTest extends AbstractCallbackTest {

    private static final String USER_AUTHORISATION = "USER_AUTH";
    private static final String USER_EMAIL = "user@email.com";
    private static final Long CASE_REFERENCE = 12345L;
    private static final String CASE_FLAG_NOTES = "Notes";
    private static final String FORENAME = "Forename";
    private static final String SURNAME = "Surname";
    private static final String FULLNAME = "Forename Surname";
    private static final DocumentReference RED_DOT_ASSESSMENT_FORM = DocumentReference.builder().build();

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseSummaryCourtGenerator caseSummaryCourtGenerator;

    CaseFlagControllerSubmittedTest() {
        super("add-case-flag");
    }

    @BeforeEach
    public void setUp() {
        when(caseSummaryCourtGenerator.generate(any())).thenReturn(SyntheticCaseSummary.builder().build());
    }


    @Test
    public void shouldUpdateSummaryCaseFields() {
        CaseData caseDataBefore = CaseData.builder()
            .redDotAssessmentForm(RED_DOT_ASSESSMENT_FORM)
            .caseFlagNotes(CASE_FLAG_NOTES)
            .build();

        CaseData caseData = CaseData.builder()
            .id(CASE_REFERENCE)
            .redDotAssessmentForm(RED_DOT_ASSESSMENT_FORM)
            .caseFlagNotes(CASE_FLAG_NOTES)
            .caseFlagAdded("Yes")
            .build();

        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder()
                .id(USER_ID)
                .surname(SURNAME)
                .forename(FORENAME)
                .email(USER_EMAIL)
                .roles(Arrays.asList("caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary"))
                .build());

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(caseData.getId()),
            eq("internal-update-case-summary"),
            anyMap());
    }
}
