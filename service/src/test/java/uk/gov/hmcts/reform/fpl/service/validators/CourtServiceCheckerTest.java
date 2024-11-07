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
import uk.gov.hmcts.reform.fpl.enums.CourtServicesNeeded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(MockitoExtension.class)
class CourtServiceCheckerTest {

    private static final CaseData ANY_CASE_DATA = mock(CaseData.class);

    @InjectMocks
    private CourtServiceChecker courtServiceChecker;

    @Test
    void testValidate() {
        assertThat(courtServiceChecker.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Nested
    class IsCompleted {

        @ParameterizedTest
        @NullSource
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.CourtServiceCheckerTest#incompleteHearingPreferences")
        void shouldReturnNonCompletedState(HearingPreferences hearingPreferences) {
            final CaseData caseData = CaseData.builder()
                .hearingPreferences(hearingPreferences)
                .build();

            final boolean isCompleted = courtServiceChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.CourtServiceCheckerTest#completeHearingPreferences")
        void shouldReturnCompletedState(HearingPreferences hearingPreferences) {
            final CaseData caseData = CaseData.builder()
                .hearingPreferences(hearingPreferences)
                .build();

            final boolean isCompleted = courtServiceChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }
    }

    @Test
    void testCompletedState() {
        assertThat(courtServiceChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    private static Stream<Arguments> incompleteHearingPreferences() {
        return Stream.of(
            HearingPreferences.builder().build(),
            completedHearingPreferences()
                .whichCourtServices(null)
                .build(),
            completedHearingPreferences()
                .whichCourtServices(emptyList())
                .build()
        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeHearingPreferences() {
        return Stream.of(
            HearingPreferences.builder()
                .whichCourtServices(List.of(CourtServicesNeeded.INTERPRETER))
                .interpreterDetails("Interpreter required")
                .build(),
            completedHearingPreferences()
                .build()
        )
            .map(Arguments::of);
    }

    private static HearingPreferences.HearingPreferencesBuilder completedHearingPreferences() {
        return HearingPreferences.builder()
            .welsh("Yes")
            .whichCourtServices(List.of(
                CourtServicesNeeded.INTERPRETER,
                CourtServicesNeeded.INTERMEDIARY,
                CourtServicesNeeded.FACILITIES_FOR_DISABILITY,
                CourtServicesNeeded.SEPARATE_WAITING_ROOMS,
                CourtServicesNeeded.SOMETHING_ELSE
            ))
            .interpreterDetails("Interpreter required")
            .intermediaryDetails("Intermediary hearing required")
            .disabilityAssistanceDetails("Learning disability")
            .separateWaitingRoomsDetails("Separate waiting room required")
            .somethingElseDetails("I need this from someone");
    }
}
