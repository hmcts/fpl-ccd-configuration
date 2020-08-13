package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CourtServiceCheckerIsStartedTest {

    @InjectMocks
    private CourtServiceChecker courtServiceChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyCourtServices")
    void shouldReturnFalseWhenEmptyCourtServices(HearingPreferences hearingPreferences) {
        final CaseData caseData = CaseData.builder()
                .hearingPreferences(hearingPreferences)
                .build();

        assertThat(courtServiceChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("updatedCourtServices")
    void shouldReturnTrueWhenCourtServicesNotEmpty(HearingPreferences hearingPreferences) {
        final CaseData caseData = CaseData.builder()
                .hearingPreferences(hearingPreferences)
                .build();

        assertThat(courtServiceChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> emptyCourtServices() {
        return Stream.of(
                HearingPreferences.builder().build(),
                HearingPreferences.builder()
                        .welsh("")
                        .interpreter("")
                        .disabilityAssistance("")
                        .extraSecurityMeasures("")
                        .somethingElse("")
                        .build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> updatedCourtServices() {
        return Stream.of(
                HearingPreferences.builder().welsh("Yes").build(),
                HearingPreferences.builder().interpreter("No").build(),
                HearingPreferences.builder().disabilityAssistance("Yes").build(),
                HearingPreferences.builder().extraSecurityMeasures("Yes").build(),
                HearingPreferences.builder().somethingElse("No").build())
                .map(Arguments::of);
    }
}
