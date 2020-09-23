package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimeNotMidnightValidatorTest extends TimeValidatorTest {

    @Test
    void shouldReturnAnErrorWhenAllTimeIsMidnight() {
        final LocalTime midnight = LocalTime.of(0, 0, 0);
        hearingBooking = HearingBooking.builder()
            .startDate(LocalDateTime.of(FUTURE.toLocalDate(), midnight))
            .endDate(FUTURE.plusDays(1))
            .build();

        final List<String> violations = validate(hearingBooking, group);

        assertThat(violations).hasSize(1).containsOnly("Enter a valid start time");
    }

    @Test
    void shouldNotReturnAnErrorWhenTimeIsNotMidnight() {
        hearingBooking = HearingBooking.builder()
            .startDate(FUTURE.plusHours(16))
            .endDate(FUTURE.plusDays(1))
            .build();

        final List<String> violations = validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }
}
