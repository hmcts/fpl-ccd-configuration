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

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CourtServiceCheckerTest {

    @InjectMocks
    private CourtServiceChecker courtServiceChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("hearingPreferences")
    void shouldReturnEmptyErrorsAndNonCompletedStateForOptionalEvent(HearingPreferences hearingPreferences) {
        final CaseData caseData = CaseData.builder()
                .hearingPreferences(hearingPreferences)
                .build();

        final List<String> errors = courtServiceChecker.validate(caseData);
        final boolean isCompleted = courtServiceChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    private static Stream<Arguments> hearingPreferences() {
        return Stream.of(
                HearingPreferences.builder().build(),
                HearingPreferences.builder()
                        .welsh("")
                        .intermediary("")
                        .disabilityAssistance("")
                        .extraSecurityMeasures("")
                        .somethingElse("")
                        .build(),
                HearingPreferences.builder()
                        .welsh("Yes")
                        .intermediary("No")
                        .disabilityAssistance("Yes")
                        .extraSecurityMeasures("No")
                        .somethingElse("Yes")
                        .build())
                .map(Arguments::of);
    }
}
