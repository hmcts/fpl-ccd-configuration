package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InternationalElementCheckerTest {

    @InjectMocks
    private InternationalElementChecker internationalElementChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("incompleteInternationalElement")
    void shouldReturnEmptyErrorsAndNonCompletedState(InternationalElement internationalElement) {
        final CaseData caseData = CaseData.builder()
            .internationalElement(internationalElement)
            .build();

        final List<String> errors = internationalElementChecker.validate(caseData);
        final boolean isCompleted = internationalElementChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    @ParameterizedTest
    @MethodSource("completeInternationalElement")
    void shouldReturnEmptyErrorsAndCompletedState(InternationalElement internationalElement) {
        final CaseData caseData = CaseData.builder()
            .internationalElement(internationalElement)
            .build();

        final List<String> errors = internationalElementChecker.validate(caseData);
        final boolean isCompleted = internationalElementChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    private static Stream<Arguments> incompleteInternationalElement() {
        return Stream.of(
            InternationalElement.builder().build(),
            InternationalElement.builder()
                .issues("")
                .issuesReason("")
                .proceedings("")
                .proceedingsReason("")
                .possibleCarer("")
                .possibleCarerReason("")
                .significantEvents("")
                .significantEventsReason("")
                .internationalAuthorityInvolvement("")
                .internationalAuthorityInvolvementDetails("")
                .build(),
            InternationalElement.builder()
                .issues("Yes")
                .proceedings("No")
                .possibleCarer("No")
                .significantEvents("No")
                .internationalAuthorityInvolvement("No")
                .build(),
            InternationalElement.builder()
                .issues("No")
                .proceedings("Yes")
                .possibleCarer("No")
                .significantEvents("No")
                .internationalAuthorityInvolvement("No")
                .build(),
            InternationalElement.builder()
                .issues("No")
                .proceedings("No")
                .possibleCarer("Yes")
                .significantEvents("No")
                .internationalAuthorityInvolvement("No")
                .build(),
            InternationalElement.builder()
                .issues("No")
                .proceedings("No")
                .possibleCarer("No")
                .significantEvents("Yes")
                .internationalAuthorityInvolvement("No")
                .build(),
            InternationalElement.builder()
                .issues("No")
                .proceedings("No")
                .possibleCarer("No")
                .significantEvents("No")
                .internationalAuthorityInvolvement("Yes")
                .build()
        )
            .map(Arguments::of);
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
            InternationalElement.builder()
                .issues("Yes")
                .issuesReason("Test")
                .proceedings("Yes")
                .proceedingsReason("Test")
                .possibleCarer("Yes")
                .possibleCarerReason("Test")
                .significantEvents("Yes")
                .significantEventsReason("Test")
                .internationalAuthorityInvolvement("Yes")
                .internationalAuthorityInvolvementDetails("Test")
                .build()
        )
            .map(Arguments::of);
    }
}
