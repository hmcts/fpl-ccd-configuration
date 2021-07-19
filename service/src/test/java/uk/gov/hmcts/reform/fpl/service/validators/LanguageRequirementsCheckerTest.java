package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(SpringExtension.class)
class LanguageRequirementsCheckerTest {

    private LanguageRequirementsChecker underTest = new LanguageRequirementsChecker();

    @Test
    void testValidate() {
        final CaseData anyCaseData = CaseData.builder()
            .languageRequirement("Yes")
            .build();

        assertThat(underTest.validate(anyCaseData)).isEmpty();
    }

    @Test
    void testCompletedState() {
        assertThat(underTest.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Test
    void shouldReturnEmptyErrorsAndCompletedState() {
        final CaseData anyCaseData = CaseData.builder()
            .languageRequirement("Yes")
            .build();

        final boolean isCompleted = underTest.isCompleted(anyCaseData);

        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnEmptyErrorsAndNonCompletedState() {
        final CaseData caseData = CaseData.builder()
            .languageRequirement(null)
            .build();

        final boolean isCompleted = underTest.isCompleted(caseData);

        assertThat(isCompleted).isFalse();
    }
}
