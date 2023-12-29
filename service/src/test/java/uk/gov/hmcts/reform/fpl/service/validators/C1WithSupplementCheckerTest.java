package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(MockitoExtension.class)
class C1WithSupplementCheckerTest {

    private static final CaseData ANY_CASE_DATA = mock(CaseData.class);

    @InjectMocks
    private C1WithSupplementChecker c1WithSupplementChecker;

    @Test
    void testValidate() {
        assertThat(c1WithSupplementChecker.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Test
    void testCompletedState() {
        assertThat(c1WithSupplementChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Nested
    class IsCompleted {

        @Test
        void shouldReturnEmptyErrorsAndNonCompletedStateIfSubmittedC1WithSupplementIsNull() {
            final CaseData caseData = CaseData.builder()
                .submittedC1WithSupplement(null)
                .build();

            final boolean isCompleted = c1WithSupplementChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @Test
        void shouldReturnEmptyErrorsAndNonCompletedState() {
            final CaseData caseData = CaseData.builder()
                .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                    .document(null)
                    .build())
                .build();

            final boolean isCompleted = c1WithSupplementChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @Test
        void shouldReturnEmptyErrorsAndCompletedState() {
            final CaseData caseData = CaseData.builder()
                .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                    .document(DocumentReference.builder().filename("ABC.docx").build())
                    .build())
                .build();

            final boolean isCompleted = c1WithSupplementChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }
    }

    @Nested
    class IsStarted {

        @Test
        void shouldReturnFalse() {
            final CaseData caseData = CaseData.builder()
                .caseName("Test")
                .build();

            assertThat(c1WithSupplementChecker.isStarted(caseData)).isFalse();
        }

        @Test
        void shouldReturnTrue() {
            final CaseData caseData = CaseData.builder()
                .caseName("Test")
                .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                    .document(DocumentReference.builder().filename("ABC.docx").build())
                    .build())
                .build();

            assertThat(c1WithSupplementChecker.isStarted(caseData)).isTrue();
        }

        @Test
        void shouldReturnFalseIfDocumentIsNull() {
            final CaseData caseData = CaseData.builder()
                .caseName("Test")
                .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                    .build())
                .build();

            assertThat(c1WithSupplementChecker.isStarted(caseData)).isFalse();
        }
    }
}
