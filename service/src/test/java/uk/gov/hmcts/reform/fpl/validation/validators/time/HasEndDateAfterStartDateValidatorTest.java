package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalValidatorFactoryBean.class})
class HasEndDateAfterStartDateValidatorTest extends TimeValidatorTest {

    @Autowired
    public HasEndDateAfterStartDateValidatorTest(Validator validator) {
        super(validator);
    }

    @Test
    void shouldReturnAnErrorWhenStartDateIsAfterEndDate() {
        hearingBooking = createHearingBooking(FUTURE, LocalDateTime.now());

        final List<String> violations = validator.validate(hearingBooking, group)
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        assertThat(violations).contains("The start date cannot be after the end date");
    }

    @Test
    void shouldReturnAnErrorWhenDatesAndTimesAreTheSame() {
        hearingBooking = createHearingBooking(FUTURE, FUTURE);

        final List<String> violations = validator.validate(hearingBooking, group)
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        assertThat(violations).contains("The start date cannot be after the end date");
    }

    @Test
    void shouldNotReturnAnErrorWhenDatesAreTheSameAndTimesAreDifferent() {
        hearingBooking = createHearingBooking(
            LocalDateTime.of(FUTURE.toLocalDate(), LocalTime.of(2, 2, 2)),
            LocalDateTime.of(FUTURE.toLocalDate(), LocalTime.of(3, 3, 3)));

        final Set<ConstraintViolation<HearingBooking>> violations = validator.validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenStartDateIsBeforeTheEndDate() {
        hearingBooking = createHearingBooking(LocalDateTime.now().plusDays(1), FUTURE);

        final Set<ConstraintViolation<HearingBooking>> violations = validator.validate(hearingBooking, group);

        assertThat(violations).isEmpty();
    }
}
