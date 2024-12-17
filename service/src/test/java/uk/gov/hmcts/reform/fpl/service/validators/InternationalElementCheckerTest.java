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
                .whichCountriesInvolved(null)
                .build(),
            completedInternationalElement()
                .whichCountriesInvolved("")
                .build(),

            completedInternationalElement()
                .outsideHagueConvention(null)
                .build(),
            completedInternationalElement()
                .outsideHagueConvention("")
                .build(),

            completedInternationalElement()
                .importantDetails(null)
                .build(),
            completedInternationalElement()
                .importantDetails("")
                .build()

        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeInternationalElement() {
        return Stream.of(
            InternationalElement.builder()
                .whichCountriesInvolved("Test")
                .outsideHagueConvention("No")
                .importantDetails("Test")
                .build(),
            completedInternationalElement()
                .build()
        ).map(Arguments::of);
    }

    private static InternationalElement.InternationalElementBuilder completedInternationalElement() {
        return InternationalElement.builder()
            .whichCountriesInvolved("Test")
            .outsideHagueConvention("Yes")
            .importantDetails("Test");
    }
}
