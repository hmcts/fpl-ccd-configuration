package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.ConstraintViolation;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static org.assertj.core.api.Assertions.assertThat;

public class TimeRangeValidatorTest extends TimeValidatorTest {

    Bar bar = new Bar();

    @Test
    void foo() {
        final Set<ConstraintViolation<Bar>> validate = validator.validate(bar);
        assertThat(validate).size().isEqualTo(1);
    }

    class Bar {
        @TimeRange(rangeBefore = @TimeDifference(amount = 2, unit = DAYS),
            rangeAfter = @TimeDifference(amount = 7, unit = MONTHS))
        public LocalDateTime now = LocalDateTime.now();
    }
}
