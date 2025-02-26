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

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InternationalElementCheckerIsStartedTest {

    @InjectMocks
    private InternationalElementChecker internationalElementChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyInternationalElement")
    void shouldReturnFalseWhenEmptyInternationalElement(InternationalElement internationalElement) {
        final CaseData caseData = CaseData.builder()
                .internationalElement(internationalElement)
                .build();

        assertThat(internationalElementChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyInternationalElement")
    void shouldReturnTrueWhenInternationalElementNotEmpty(InternationalElement internationalElement) {
        final CaseData caseData = CaseData.builder()
                .internationalElement(internationalElement)
                .build();

        assertThat(internationalElementChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> nonEmptyInternationalElement() {
        return Stream.of(
                InternationalElement.builder()
                        .whichCountriesInvolved("Test").build(),
                InternationalElement.builder()
                        .outsideHagueConvention("No").build(),
                InternationalElement.builder()
                        .importantDetails("Test").build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyInternationalElement() {
        return Stream.of(
                InternationalElement.builder()
                        .build(),
                InternationalElement.builder()
                        .whichCountriesInvolved("")
                        .outsideHagueConvention("")
                        .importantDetails("")
                        .build())
                .map(Arguments::of);
    }
}
