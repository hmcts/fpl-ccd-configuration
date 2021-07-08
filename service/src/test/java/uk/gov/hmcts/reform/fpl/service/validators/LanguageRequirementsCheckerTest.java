package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(MockitoExtension.class)
class LanguageRequirementsCheckerTest {

    private static final CaseData ANY_CASE_DATA = mock(CaseData.class);

    @InjectMocks
    private LanguageRequirementsChecker languageRequirementsChecker;

    @Test
    void testValidate() {
        assertThat(languageRequirementsChecker.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Test
    void testCompletedState() {
        assertThat(languageRequirementsChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Test
    void shouldReturnEmptyErrorsAndCompletedState() {
        final CaseData caseData = CaseData.builder()
            .languageRequirement("Yes")
            .build();

        final boolean isCompleted = languageRequirementsChecker.isCompleted(caseData);

        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnEmptyErrorsAndNonCompletedState() {
        final CaseData caseData = CaseData.builder()
            .languageRequirement(null)
            .build();

        final boolean isCompleted = languageRequirementsChecker.isCompleted(caseData);

        assertThat(isCompleted).isFalse();
    }

}
