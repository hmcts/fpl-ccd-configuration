package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@WebMvcTest(CaseFlagController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseFlagControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final String USER_EMAIL = "user@email.com";
    private static final Long CASE_REFERENCE = 12345L;
    private static final String CASE_FLAG_NOTES = "Notes";
    private static final String FORENAME = "Forename";
    private static final String SURNAME = "Surname";
    private static final DocumentReference RED_DOT_ASSESSMENT_FORM = DocumentReference.builder().build();

    @Autowired
    CaseFlagController caseFlagController;

    CaseFlagControllerAboutToSubmitTest() {
        super("add-case-flag");
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

        doReturn(UserDetails.builder()
            .id(USER_ID)
            .surname(SURNAME)
            .forename(FORENAME)
            .email(USER_EMAIL)
            .roles(Arrays.asList("caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary"))
            .build()).when(idamClient).getUserDetails(any());

        AboutToStartOrSubmitCallbackResponse resp = postAboutToSubmitEvent(toCallBackRequest(caseData, caseDataBefore));

        assertThat(resp.getData())
            .extracting("caseSummaryFlagAssessmentForm", "caseSummaryCaseFlagNotes",
                "caseSummaryFlagAddedByFullName", "caseSummaryFlagAddedByEmail")
            .doesNotContainNull();
    }
}
