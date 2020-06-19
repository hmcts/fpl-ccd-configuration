package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

class HasEndDateAfterStartDateValidatorTest extends TimeValidatorTest {

    @Test
    void shouldReturnAnErrorWhenStartDateIsAfterEndDate() {
        hearingBooking = createHearingBooking(FUTURE, LocalDateTime.now());

        final List<String> violations = validate(hearingBooking, group);

        assertThat(violations).contains("The start date cannot be after the end date");
    }

    @Test
    void shouldReturnAnErrorWhenDatesAndTimesAreTheSame() {
        hearingBooking = createHearingBooking(FUTURE, FUTURE);

        final List<String> violations = validate(hearingBooking, group);

        assertThat(violations).contains("The start date cannot be after the end date");
    }

    @Test
    void shouldNotReturnAnErrorWhenDatesAreTheSameAndTimesAreDifferent() {
        hearingBooking = createHearingBooking(
            LocalDateTime.of(FUTURE.toLocalDate(), LocalTime.of(2, 2, 2)),
            LocalDateTime.of(FUTURE.toLocalDate(), LocalTime.of(3, 3, 3)));

        final List<String> violations = validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenStartDateIsBeforeTheEndDate() {
        hearingBooking = createHearingBooking(LocalDateTime.now().plusDays(1), FUTURE);

        final List<String> violations = validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }
}
