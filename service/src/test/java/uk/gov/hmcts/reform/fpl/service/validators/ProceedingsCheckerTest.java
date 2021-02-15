package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(MockitoExtension.class)
class ProceedingsCheckerTest {

    private static final CaseData ANY_CASE_DATA = mock(CaseData.class);

    @InjectMocks
    private ProceedingsChecker proceedingsChecker;

    @Test
    void testValidate() {
        assertThat(proceedingsChecker.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Test
    void testCompletedState() {
        assertThat(proceedingsChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Nested
    class IsCompleted {

        @ParameterizedTest
        @NullSource
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.ProceedingsCheckerTest#incompleteProceedings")
        void shouldReturnEmptyErrorsAndNonCompletedState(Proceeding proceeding) {
            final CaseData caseData = CaseData.builder()
                .proceeding(proceeding)
                .build();

            final boolean isCompleted = proceedingsChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.ProceedingsCheckerTest#completeProceedings")
        void shouldReturnEmptyErrorsAndCompletedState(Proceeding proceeding) {
            final CaseData caseData = CaseData.builder()
                .proceeding(proceeding)
                .build();

            final boolean isCompleted = proceedingsChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }
    }

    private static Stream<Arguments> incompleteProceedings() {
        return Stream.of(
            Proceeding.builder()
                .build(),

            completedProceeding()
                .onGoingProceeding(null)
                .build(),
            completedProceeding()
                .onGoingProceeding("")
                .build(),

            completedProceeding()
                .proceedingStatus(null)
                .build(),
            completedProceeding()
                .proceedingStatus("")
                .build(),

            completedProceeding()
                .caseNumber(null)
                .build(),
            completedProceeding()
                .caseNumber("")
                .build(),

            completedProceeding()
                .started(null)
                .build(),
            completedProceeding()
                .started("")
                .build(),

            completedProceeding()
                .ended(null)
                .build(),
            completedProceeding()
                .ended("")
                .build(),

            completedProceeding()
                .ordersMade(null)
                .build(),
            completedProceeding()
                .ordersMade("")
                .build(),

            completedProceeding()
                .children(null)
                .build(),
            completedProceeding()
                .children("")
                .build(),

            completedProceeding()
                .guardian(null)
                .build(),
            completedProceeding()
                .guardian("")
                .build(),

            completedProceeding()
                .sameGuardianNeeded("No")
                .sameGuardianDetails(null)
                .build(),

            completedProceeding()
                .sameGuardianNeeded("No")
                .sameGuardianDetails("")
                .build()
        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeProceedings() {
        return Stream.of(
            Proceeding.builder()
                .onGoingProceeding("No")
                .build(),
            Proceeding.builder()
                .onGoingProceeding("DontKnow")
                .build(),
            completedProceeding()
                .build()
        ).map(Arguments::of);
    }

    private static Proceeding.ProceedingBuilder completedProceeding() {
        return Proceeding.builder()
            .onGoingProceeding("Yes")
            .proceedingStatus("Test")
            .caseNumber("Test")
            .started("Test")
            .ended("Test")
            .ordersMade("Test")
            .children("Test")
            .guardian("Test");
    }
}
