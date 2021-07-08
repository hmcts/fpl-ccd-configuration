package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LanguageRequirementsChecker.class)
class LanguageRequirementsCheckerTest {

    @Autowired
    private LanguageRequirementsChecker underTest;

    private static final CaseData ANY_CASE_DATA = CaseData
        .builder()
        .languageRequirement("Yes")
        .build();

    @Test
    void testValidate() {
        assertThat(underTest.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Test
    void testCompletedState() {
        assertThat(underTest.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Test
    void shouldReturnEmptyErrorsAndCompletedState() {
        final boolean isCompleted = underTest.isCompleted(ANY_CASE_DATA);

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
