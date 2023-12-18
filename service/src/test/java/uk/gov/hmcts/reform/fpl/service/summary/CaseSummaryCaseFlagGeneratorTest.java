package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(MockitoExtension.class)
class CaseSummaryCaseFlagGeneratorTest {

    private static final String USER_EMAIL = "user@email.com";
    private static final String CASE_FLAG_NOTES = "Notes";
    private static final String FULLNAME = "Forename Surname";
    private static final DocumentReference RED_DOT_ASSESSMENT_FORM = DocumentReference.builder().build();

    @Mock
    UserService userService;

    @InjectMocks
    CaseSummaryCaseFlagGenerator underTest;

    @Test
    public void shouldBuildCaseDataWithCaseFlagDetails() {
        CaseData caseData = CaseData.builder()
            .redDotAssessmentForm(RED_DOT_ASSESSMENT_FORM)
            .caseFlagNotes(CASE_FLAG_NOTES)
            .caseFlagValueUpdated(YES)
            .build();

        when(userService.getUserName()).thenReturn(FULLNAME);
        when(userService.getUserEmail()).thenReturn(USER_EMAIL);

        SyntheticCaseSummary actual = underTest.generate(caseData);
        SyntheticCaseSummary expected = SyntheticCaseSummary.builder()
            .caseSummaryFlagAssessmentForm(RED_DOT_ASSESSMENT_FORM)
            .caseSummaryCaseFlagNotes(CASE_FLAG_NOTES)
            .caseSummaryFlagAddedByEmail(USER_EMAIL)
            .caseSummaryFlagAddedByFullName(FULLNAME)
            .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldBuildCaseDataWithoutUpdatingCaseFlagUserDetails() {
        CaseData caseData = CaseData.builder()
            .redDotAssessmentForm(RED_DOT_ASSESSMENT_FORM)
            .caseFlagNotes(CASE_FLAG_NOTES)
            .caseFlagValueUpdated(NO)
            .build();

        SyntheticCaseSummary actual = underTest.generate(caseData);
        SyntheticCaseSummary expected = SyntheticCaseSummary.builder()
            .caseSummaryFlagAssessmentForm(RED_DOT_ASSESSMENT_FORM)
            .caseSummaryCaseFlagNotes(CASE_FLAG_NOTES)
            .caseSummaryFlagAddedByEmail(null)
            .caseSummaryFlagAddedByFullName(null)
            .build();

        assertThat(actual).isEqualTo(expected);
        verifyNoInteractions(userService);
    }

    @Test
    public void shouldBuildCaseDetailsMapWithCaseFlagDetails() {
        CaseData caseData = CaseData.builder()
            .redDotAssessmentForm(RED_DOT_ASSESSMENT_FORM)
            .caseFlagNotes(CASE_FLAG_NOTES)
            .caseFlagValueUpdated(YES)
            .build();

        when(userService.getUserName()).thenReturn(FULLNAME);
        when(userService.getUserEmail()).thenReturn(USER_EMAIL);

        Map<String, Object> actual = underTest.generateFields(caseData);
        Map<String, Object> expected = Map.of(
            "caseSummaryFlagAssessmentForm", RED_DOT_ASSESSMENT_FORM,
            "caseSummaryCaseFlagNotes", CASE_FLAG_NOTES,
            "caseSummaryFlagAddedByEmail", USER_EMAIL,
            "caseSummaryFlagAddedByFullName", FULLNAME
        );

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldNoThrowNullPointerExceptionIfMissingOptionalFields() {
        CaseData caseData = CaseData.builder()
            .redDotAssessmentForm(RED_DOT_ASSESSMENT_FORM)
            .caseFlagValueUpdated(YES)
            .build();

        when(userService.getUserName()).thenReturn(FULLNAME);
        when(userService.getUserEmail()).thenReturn(USER_EMAIL);

        assertDoesNotThrow(() -> (underTest.generateFields(caseData)));
    }

}
