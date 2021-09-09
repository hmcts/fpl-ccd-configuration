package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.HOURS_MINS;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PastHearingDatesValidatorService.class)
class PastHearingDatesValidatorServiceTest {
    @Autowired
    private PastHearingDatesValidatorService service;

    @Test
    void shouldReturnEmptyValidationErrorsWhenNoHearingDate() {
        final List<String> validationErrors = service.validateHearingDates(null, null);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnEmptyValidationErrorsWhenHearingDateTimeIsValid() {
        LocalDateTime hearingStartDate = LocalDateTime.now();
        LocalDateTime hearingEndDate = hearingStartDate.plusHours(1);
        final List<String> validationErrors = service.validateHearingDates(hearingStartDate, hearingEndDate);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorsWhenHearingDateTimeIsNotValid() {
        LocalDateTime hearingStartDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDateTime hearingEndDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        final List<String> validationErrors = service.validateHearingDates(hearingStartDate, hearingEndDate);

        assertThat(validationErrors).containsExactlyInAnyOrder("Enter a valid start time", "Enter a valid end time");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validateHearingDatesSource")
    void shouldReturnValidationErrorWhenHearingEndDateIsNotAfterTheStartDate(
        String testName, LocalDateTime hearingStartDate, LocalDateTime hearingEndDate
    ) {
        final List<String> validationErrors = service.validateHearingDates(hearingStartDate, hearingEndDate);

        assertThat(validationErrors)
            .containsOnly("The end date and time must be after the start date and time");
    }

    @ParameterizedTest
    @ValueSource(ints = { 0,-1})
    void shouldReturnValidationErrorWhenHearingDaysIsNegativeOrZero(int days) {
        final List<String> validationErrors = service.validateDays(DAYS.getType(), days);

        assertThat(validationErrors)
            .containsOnly("Enter valid days");
    }

    @Test
    void shouldReturnNoErrorsWhenHearingDaysIsValid() {
        final List<String> validationErrors = service.validateDays(DAYS.getType(), 10);

        assertThat(validationErrors).isEmpty();
    }


    @ParameterizedTest
    @MethodSource("invalidHoursMinutesSource")
    void shouldReturnValidationErrorWhenHearingHoursOrMinutesAreInvalid(int hours, int minutes) {
        final List<String> validationErrors = service.validateHoursMinutes(HOURS_MINS.getType(), hours, minutes);

        assertThat(validationErrors)
            .containsOnly("Enter valid hours and minutes");
    }

    @Test
    void shouldReturnNoErrorsWhenHearingHoursOrMinutesAreValid() {
        final List<String> validationErrors = service.validateHoursMinutes(HOURS_MINS.getType(), 2, 4);

        assertThat(validationErrors).isEmpty();
    }

    private static Stream<Arguments> invalidHoursMinutesSource() {
        return Stream.of(
            Arguments.of(0,0),
            Arguments.of(-1,0),
            Arguments.of(-1,-1),
            Arguments.of(0,-1)
        );
    }

    private static Stream<Arguments> validateHearingDatesSource() {
        LocalDateTime startTime = LocalDateTime.of(2020, 12, 10, 10, 10, 10);
        return Stream.of(
            Arguments.of("Hearing end date is before start date", startTime, startTime.minusDays(1)),
            Arguments.of("Hearing end time is 1 second before start time", startTime, startTime.minusSeconds(1)),
            Arguments.of("Hearing end time is 1 minute before start time", startTime, startTime.minusMinutes(1)),
            Arguments.of("Hearing end date time and start date time are same", startTime, startTime)
        );
    }


}
