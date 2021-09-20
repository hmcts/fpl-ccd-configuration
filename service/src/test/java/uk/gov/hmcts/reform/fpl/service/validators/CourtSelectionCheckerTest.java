package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(MockitoExtension.class)
class CourtSelectionCheckerTest {

    private final Court court = Court.builder()
        .name("Court 1")
        .email("court@tst.com")
        .code("123")
        .build();

    @InjectMocks
    private CourtSelectionChecker courtSelectionChecker;

    @Nested
    class IsValid {

        @Test
        void shouldReturnValidationErrorWhenCourtNotSelected() {
            CaseData caseData = CaseData.builder()
                .multiCourts(YES)
                .court(null)
                .build();

            assertThat(courtSelectionChecker.validate(caseData)).containsExactly("Select court");
        }

        @Test
        void shouldReturnEmptyErrorsWhenCourtIsSelected() {
            CaseData caseData = CaseData.builder()
                .multiCourts(YES)
                .court(court)
                .build();

            assertThat(courtSelectionChecker.validate(caseData)).isEmpty();
        }

        @Test
        void shouldReturnErrorsWhenSingleCourtAvailable() {
            CaseData caseData = CaseData.builder()
                .multiCourts(NO)
                .build();

            assertThat(courtSelectionChecker.validate(caseData)).isEmpty();
        }
    }

    @Nested
    class IsCompleted {

        @Test
        void shouldReturnTrueWhenCaseHasDesignatedCourtSelected() {
            final CaseData caseData = CaseData.builder()
                .court(court)
                .build();

            assertThat(courtSelectionChecker.isCompleted(caseData)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCaseHasNotDesignatedCourtSelected() {
            final CaseData caseData = CaseData.builder()
                .court(null)
                .build();

            assertThat(courtSelectionChecker.isCompleted(caseData)).isFalse();
        }
    }

    @Nested
    class IsStarted {

        @Test
        void shouldReturnTrueWhenCaseHasDesignatedCourtSelected() {
            final CaseData caseData = CaseData.builder()
                .court(court)
                .build();

            assertThat(courtSelectionChecker.isStarted(caseData)).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCaseHasNotDesignatedCourtSelected() {
            final CaseData caseData = CaseData.builder()
                .court(null)
                .build();

            assertThat(courtSelectionChecker.isStarted(caseData)).isFalse();
        }
    }

    @Test
    void shouldMarkCompletedTaskAsFinished() {
        assertThat(courtSelectionChecker.completedState()).isEqualTo(TaskState.COMPLETED_FINISHED);
    }
}
