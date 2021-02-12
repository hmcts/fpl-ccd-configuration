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
import uk.gov.hmcts.reform.fpl.model.InternationalElement;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(MockitoExtension.class)
class InternationalElementCheckerTest {

    private static final CaseData ANY_CASE_DATA = mock(CaseData.class);

    @InjectMocks
    private InternationalElementChecker internationalElementChecker;

    @Test
    void testValidate() {
        assertThat(internationalElementChecker.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Test
    void testCompletedState() {
        assertThat(internationalElementChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Nested
    class IsCompleted {

        @ParameterizedTest
        @NullSource
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators"
            + ".InternationalElementCheckerTest#incompleteInternationalElement")
        void shouldReturnEmptyErrorsAndNonCompletedState(InternationalElement internationalElement) {
            final CaseData caseData = CaseData.builder()
                .internationalElement(internationalElement)
                .build();

            final boolean isCompleted = internationalElementChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators"
            + ".InternationalElementCheckerTest#completeInternationalElement")
        void shouldReturnEmptyErrorsAndCompletedState(InternationalElement internationalElement) {
            final CaseData caseData = CaseData.builder()
                .internationalElement(internationalElement)
                .build();

            final boolean isCompleted = internationalElementChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }
    }

    private static Stream<Arguments> incompleteInternationalElement() {
        return Stream.of(
            InternationalElement.builder().build(),

            completedInternationalElement()
                .possibleCarer(null)
                .build(),
            completedInternationalElement()
                .possibleCarer("")
                .build(),
            completedInternationalElement()
                .possibleCarer("Yes")
                .possibleCarerReason(null)
                .build(),
            completedInternationalElement()
                .possibleCarer("Yes")
                .possibleCarerReason("")
                .build(),

            completedInternationalElement()
                .significantEvents(null)
                .build(),
            completedInternationalElement()
                .significantEvents("")
                .build(),
            completedInternationalElement()
                .significantEvents("Yes")
                .significantEventsReason(null)
                .build(),
            completedInternationalElement()
                .significantEvents("Yes")
                .significantEventsReason("")
                .build(),

            completedInternationalElement()
                .issues(null)
                .build(),
            completedInternationalElement()
                .issues("")
                .build(),
            completedInternationalElement()
                .issues("Yes")
                .issuesReason(null)
                .build(),
            completedInternationalElement()
                .issues("Yes")
                .issuesReason("")
                .build(),

            completedInternationalElement()
                .proceedings(null)
                .build(),
            completedInternationalElement()
                .proceedings("")
                .build(),
            completedInternationalElement()
                .proceedings("Yes")
                .proceedingsReason(null)
                .build(),
            completedInternationalElement()
                .proceedings("Yes")
                .proceedingsReason("")
                .build(),

            completedInternationalElement()
                .internationalAuthorityInvolvement(null)
                .build(),
            completedInternationalElement()
                .internationalAuthorityInvolvement("")
                .build(),
            completedInternationalElement()
                .internationalAuthorityInvolvement("Yes")
                .internationalAuthorityInvolvementDetails(null)
                .build(),
            completedInternationalElement()
                .internationalAuthorityInvolvement("Yes")
                .internationalAuthorityInvolvementDetails("")
                .build()

        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeInternationalElement() {
        return Stream.of(
            InternationalElement.builder()
                .issues("No")
                .proceedings("No")
                .possibleCarer("No")
                .significantEvents("No")
                .internationalAuthorityInvolvement("No")
                .build(),
            completedInternationalElement()
                .build()
        ).map(Arguments::of);
    }

    private static InternationalElement.InternationalElementBuilder completedInternationalElement() {
        return InternationalElement.builder()
            .issues("Yes")
            .issuesReason("Test")
            .proceedings("Yes")
            .proceedingsReason("Test")
            .possibleCarer("Yes")
            .possibleCarerReason("Test")
            .significantEvents("Yes")
            .significantEventsReason("Test")
            .internationalAuthorityInvolvement("Yes")
            .internationalAuthorityInvolvementDetails("Test");
    }
}
