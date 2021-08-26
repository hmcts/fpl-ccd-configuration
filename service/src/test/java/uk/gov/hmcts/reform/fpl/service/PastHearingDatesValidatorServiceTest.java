package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void shouldReturnEmptyValidationErrorsWhenValidationDateIsValid() {
        LocalDateTime hearingEndDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDate vacatedDate = LocalDate.now().minusDays(1);

        final List<String> validationErrors = service.validateVacatedDate(hearingEndDate, vacatedDate);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorWhenVacatedDateIsNotAfterTheEndDate() {
        LocalDateTime hearingEndDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDate vacatedDate = LocalDate.now().plusDays(1);

        final List<String> validationErrors = service.validateVacatedDate(hearingEndDate, vacatedDate);

        assertThat(validationErrors)
            .containsOnly("The vacated date must be before, or the same as the hearing date.");
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
