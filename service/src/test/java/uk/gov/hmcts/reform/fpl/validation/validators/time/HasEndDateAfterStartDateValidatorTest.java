package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasEndDateAfterStartDateValidatorTest extends TimeValidatorTest {

    @Test
    void shouldReturnAnErrorWhenStartDateIsAfterEndDate() {
        hearingBooking = HearingBooking.builder()
            .startDate(FUTURE)
            .endDate(LocalDateTime.now().plusDays(1))
            .build();

        final List<String> violations = validator.validate(hearingBooking, group)
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        assertThat(violations).hasSize(1).containsOnlyOnce("The start date cannot be after the end date");
    }

    @Test
    void shouldReturnAnErrorWhenDatesAreTheSame() {
        hearingBooking = HearingBooking.builder()
            .startDate(FUTURE)
            .endDate(FUTURE)
            .build();

        final List<String> violations = validator.validate(hearingBooking, group)
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        assertThat(violations).hasSize(1).containsOnlyOnce("The start date cannot be after the end date");
    }

    @Test
    void shouldNotReturnAnErrorWhenStartDateIsBeforeTheEndDate() {
        hearingBooking = HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(1))
            .endDate(FUTURE)
            .build();

        final Set<ConstraintViolation<HearingBooking>> violations = validator.validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }
}
