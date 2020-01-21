package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class, LocalValidatorFactoryBean.class})
public class TimeRangeValidatorTest extends TimeValidatorTest {

    private final Time time;

    @Autowired
    public TimeRangeValidatorTest(Validator validator, Time time) {
        super(validator);
        this.time = time;
    }

    @Test
    void shouldReturnAnErrorWhenDateTimeExceedsRange() {
        InvalidTimeRangeValidation invalidTimeRangeValidation = new InvalidTimeRangeValidation(time);
        final Set<ConstraintViolation<InvalidTimeRangeValidation>> validate =
            validator.validate(invalidTimeRangeValidation);
        assertThat(validate).size().isEqualTo(1);
    }

    @Test
    void shouldNotReturnAnErrorWhenDateTimeDoesNotExceedRange() {
        ValidTimeRangeValidation validTimeRangeValidation = new ValidTimeRangeValidation(time);
        final Set<ConstraintViolation<ValidTimeRangeValidation>> validate =
            validator.validate(validTimeRangeValidation);
        assertThat(validate).size().isEqualTo(0);
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
