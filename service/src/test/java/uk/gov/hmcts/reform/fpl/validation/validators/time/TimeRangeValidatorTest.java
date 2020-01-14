package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.ConstraintViolation;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

public class TimeRangeValidatorTest extends TimeValidatorTest {

    ValidTimeRangeValidation validTimeRangeValidation = new ValidTimeRangeValidation();
    InvalidTimeRangeValidator invalidTimeRangeValidatorB = new InvalidTimeRangeValidator();

    @Test
    void shouldReturnAnErrorWhenDateTimeExceedsRange() {
        final Set<ConstraintViolation<InvalidTimeRangeValidator>> validate =
            validator.validate(invalidTimeRangeValidatorB);
        assertThat(validate).size().isEqualTo(1);
    }

    @Test
    void shouldNotReturnAnErrorWhenDateTimeDoesNotExceedRange() {
        final Set<ConstraintViolation<ValidTimeRangeValidation>> validate =
            validator.validate(validTimeRangeValidation);
        assertThat(validate).size().isEqualTo(0);
    }

    class ValidTimeRangeValidation {
        @TimeRange(rangeAfter = @TimeDifference(amount = 7, unit = DAYS))
        public LocalDateTime now = LocalDateTime.now().plusDays(2);
    }

    class InvalidTimeRangeValidator {
        @TimeRange(rangeAfter = @TimeDifference(amount = 7, unit = DAYS))
        public LocalDateTime now = LocalDateTime.now().plusDays(9);
    }
}
