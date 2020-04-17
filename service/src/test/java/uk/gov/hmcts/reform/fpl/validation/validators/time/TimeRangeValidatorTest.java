package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {FixedTimeConfiguration.class})
public class TimeRangeValidatorTest extends TimeValidatorTest {

    @Autowired
    private Time time;

    @Test
    void shouldReturnAnErrorWhenDateTimeExceedsRange() {
        InvalidTimeRangeValidation invalidTimeRangeValidation = new InvalidTimeRangeValidation(time);
        final List<String> violations = validate(invalidTimeRangeValidation);
        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldNotReturnAnErrorWhenDateTimeDoesNotExceedRange() {
        ValidTimeRangeValidation validTimeRangeValidation = new ValidTimeRangeValidation(time);
        final List<String> violations = validate(validTimeRangeValidation);
        assertThat(violations).isEmpty();
    }

    class ValidTimeRangeValidation {
        @TimeRange(maxDate = @TimeDifference(amount = 7, unit = DAYS))
        final LocalDateTime nowPlusTwoDays;

        ValidTimeRangeValidation(Time time) {
            nowPlusTwoDays = time.now().plusDays(2);
        }
    }

    class InvalidTimeRangeValidation {
        @TimeRange(maxDate = @TimeDifference(amount = 7, unit = DAYS))
        final LocalDateTime nowPlusNineDays;

        InvalidTimeRangeValidation(Time time) {
            nowPlusNineDays = time.now().plusDays(9);
        }
    }
}
