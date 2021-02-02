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
    @MethodSource("internationalElement")
    void shouldReturnEmptyErrorsAndNonCompletedStateForOptionalEvent(InternationalElement internationalElement) {
        final CaseData caseData = CaseData.builder()
                .internationalElement(internationalElement)
                .build();

        final List<String> errors = internationalElementChecker.validate(caseData);
        final boolean isCompleted = internationalElementChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    private static Stream<Arguments> internationalElement() {
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
                        .build())
                .map(Arguments::of);
    }
}
