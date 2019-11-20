package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasFutureDateValidatorTest extends TimeValidatorTest {

    @Test
    void shouldReturnAnErrorIfDateIsInThePast() {
        final LocalDateTime past = LocalDateTime.now().minusDays(1);
        hearingBooking = HearingBooking.builder().startDate(past).endDate(FUTURE).build();

        final Set<String> violations = validator.validate(hearingBooking, group)
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toSet());

        assertThat(violations).hasSize(1).containsOnly("Enter a start date in the future");
    }

    @Test
    void shouldNotReturnAnErrorIfDateIsTodayButTimeIsFuture() {
        hearingBooking = HearingBooking.builder().startDate(LocalDateTime.now().plusMinutes(1)).endDate(FUTURE).build();

        final Set<ConstraintViolation<HearingBooking>> violations = validator.validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorIfDateIsInTheFuture() {
        hearingBooking = HearingBooking.builder().startDate(FUTURE).endDate(FUTURE.plusDays(1)).build();

        final Set<ConstraintViolation<HearingBooking>> violations = validator.validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }
}
