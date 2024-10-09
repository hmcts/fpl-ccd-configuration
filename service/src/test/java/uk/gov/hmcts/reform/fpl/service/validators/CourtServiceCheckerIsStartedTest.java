package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CourtServicesNeeded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
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
                        .whichCourtServices(emptyList())
                        .interpreterDetails("")
                        .intermediaryDetails("")
                        .disabilityAssistance("")
                        .separateWaitingRoomsDetails("")
                        .somethingElseDetails("")
                        .build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> updatedCourtServices() {
        return Stream.of(
                HearingPreferences.builder().whichCourtServices(List.of(
                        CourtServicesNeeded.INTERPRETER,
                        CourtServicesNeeded.INTERMEDIARY,
                        CourtServicesNeeded.FACILITIES_FOR_DISABILITY,
                        CourtServicesNeeded.SEPARATE_WAITING_ROOMS,
                        CourtServicesNeeded.SOMETHING_ELSE)).build(),
                HearingPreferences.builder().interpreterDetails("Interpreter required").build(),
                HearingPreferences.builder().intermediaryDetails("Intermediary hearing required").build(),
                HearingPreferences.builder().disabilityAssistanceDetails("Learning disability").build(),
                HearingPreferences.builder().separateWaitingRoomsDetails("Separate waiting room required").build(),
                HearingPreferences.builder().somethingElseDetails("I need this from someone").build())
                .map(Arguments::of);
    }
}
