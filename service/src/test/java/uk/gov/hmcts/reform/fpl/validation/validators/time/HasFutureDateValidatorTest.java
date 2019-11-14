package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.interfaces.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

class HasFutureDateValidatorTest {

    private static final LocalDateTime FUTURE = LocalDateTime.now().plusDays(20);
    private static Validator validator;
    private HearingBooking hearingBooking;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldReturnAnErrorIfDateIsInThePast() {
        final LocalDateTime past = LocalDateTime.now().minusDays(1);
        hearingBooking = HearingBooking.builder().startDate(past).endDate(FUTURE).build();

        final Set<String> violations = validator.validate(hearingBooking, HearingBookingDetailsGroup.class)
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toSet());

        assertThat(violations).hasSize(1).containsOnly("Enter a start date in the future");
    }

    @Test
    void shouldNotReturnAnErrorIfDateIsToday() {
        // FIXME: 14/11/2019 Date time has nanosecond precision, need the mocking that joe provides to get this to
        //  work properly
        hearingBooking = HearingBooking.builder().startDate(LocalDateTime.now()).endDate(FUTURE).build();

        final Set<ConstraintViolation<HearingBooking>> violations = validator.validate(hearingBooking,
            HearingBookingDetailsGroup.class);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorIfDateIsInTheFuture() {
        hearingBooking = HearingBooking.builder().startDate(FUTURE).endDate(FUTURE).build();

        final Set<ConstraintViolation<HearingBooking>> violations = validator.validate(hearingBooking,
            HearingBookingDetailsGroup.class);

        assertThat(violations).isEmpty();
    }
}
