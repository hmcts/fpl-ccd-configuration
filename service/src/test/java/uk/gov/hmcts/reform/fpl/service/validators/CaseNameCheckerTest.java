package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseNameChecker.class, LocalValidatorFactoryBean.class})
class CaseNameCheckerTest {

    @Autowired
    private CaseNameChecker caseNameChecker;

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnErrorWhenNoCaseName(String caseName) {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = caseNameChecker.validate(caseData);
        final boolean isCompleted = caseNameChecker.isCompleted(caseData);

        assertThat(errors).contains("Enter a case name|Rhowch enw’r achos");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenCaseNameIsPresent() {
        final CaseData caseData = CaseData.builder()
                .caseName("test")
                .build();

        final List<String> errors = caseNameChecker.validate(caseData);
        final boolean isCompleted = caseNameChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void testCompletedState() {
        assertThat(caseNameChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }
}
