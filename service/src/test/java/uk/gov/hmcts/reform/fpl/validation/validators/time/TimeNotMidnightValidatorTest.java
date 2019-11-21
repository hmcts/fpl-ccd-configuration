package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class TimeNotMidnightValidatorTest extends TimeValidatorTest {

    @Test
    void shouldReturnAnErrorWhenAllTimeIsMidnight() {
        final LocalTime midnight = LocalTime.of(0, 0, 0);
        hearingBooking = HearingBooking.builder()
            .startDate(LocalDateTime.of(FUTURE.toLocalDate(), midnight))
            .endDate(FUTURE.plusDays(1))
            .build();

        final Set<String> violations = validator.validate(hearingBooking, group)
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toSet());

        assertThat(violations).hasSize(1).containsOnly("Enter a valid start time");
    }

    @Test
    void shouldNotReturnAnErrorWhenTimeIsNotMidnight() {
        hearingBooking = HearingBooking.builder()
            .startDate(FUTURE.plusHours(16))
            .endDate(FUTURE.plusDays(1))
            .build();

        final Set<ConstraintViolation<HearingBooking>> violations = validator.validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }
}
