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
    @MethodSource("incompleteHearingPreferences")
    void shouldReturnEmptyErrorsAndNonCompletedState(HearingPreferences hearingPreferences) {
        final CaseData caseData = CaseData.builder()
            .hearingPreferences(hearingPreferences)
            .build();

        final List<String> errors = courtServiceChecker.validate(caseData);
        final boolean isCompleted = courtServiceChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    @ParameterizedTest
    @MethodSource("completeHearingPreferences")
    void shouldReturnEmptyErrorsAndCompletedState(HearingPreferences hearingPreferences) {
        final CaseData caseData = CaseData.builder()
            .hearingPreferences(hearingPreferences)
            .build();

        final List<String> errors = courtServiceChecker.validate(caseData);
        final boolean isCompleted = courtServiceChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    private static Stream<Arguments> incompleteHearingPreferences() {
        return Stream.of(
            HearingPreferences.builder().build(),
            HearingPreferences.builder()
                .welsh("")
                .interpreter("")
                .intermediary("")
                .disabilityAssistance("")
                .extraSecurityMeasures("")
                .somethingElse("")
                .build(),
            HearingPreferences.builder()
                .welsh("Yes")
                .interpreter("No")
                .intermediary("No")
                .disabilityAssistance("No")
                .extraSecurityMeasures("No")
                .somethingElse("No")
                .build(),
            HearingPreferences.builder()
                .welsh("No")
                .interpreter("Yes")
                .intermediary("No")
                .disabilityAssistance("No")
                .extraSecurityMeasures("No")
                .somethingElse("No")
                .build(),
            HearingPreferences.builder()
                .welsh("No")
                .interpreter("No")
                .intermediary("Yes")
                .disabilityAssistance("No")
                .extraSecurityMeasures("No")
                .somethingElse("No")
                .build(),
            HearingPreferences.builder()
                .welsh("No")
                .interpreter("No")
                .intermediary("No")
                .disabilityAssistance("Yes")
                .extraSecurityMeasures("No")
                .somethingElse("No")
                .build(),
            HearingPreferences.builder()
                .welsh("No")
                .interpreter("No")
                .intermediary("No")
                .disabilityAssistance("No")
                .extraSecurityMeasures("Yes")
                .somethingElse("No")
                .build(),
            HearingPreferences.builder()
                .welsh("No")
                .interpreter("No")
                .intermediary("No")
                .disabilityAssistance("No")
                .extraSecurityMeasures("No")
                .somethingElse("Yes")
                .build()
            )
            .map(Arguments::of);
    }

    private static Stream<Arguments> completeHearingPreferences() {
        return Stream.of(
            HearingPreferences.builder()
                .welsh("No")
                .interpreter("No")
                .intermediary("No")
                .disabilityAssistance("No")
                .extraSecurityMeasures("No")
                .somethingElse("No")
                .build(),
            HearingPreferences.builder()
                .welsh("Yes")
                .welshDetails("Test")
                .interpreter("Yes")
                .interpreterDetails("Yes")
                .intermediary("Yes")
                .intermediaryDetails("Test")
                .disabilityAssistance("Yes")
                .disabilityAssistanceDetails("Test")
                .extraSecurityMeasures("Yes")
                .extraSecurityMeasuresDetails("Test")
                .somethingElse("Yes")
                .somethingElseDetails("Test")
                .build()
        )
            .map(Arguments::of);
    }
}
