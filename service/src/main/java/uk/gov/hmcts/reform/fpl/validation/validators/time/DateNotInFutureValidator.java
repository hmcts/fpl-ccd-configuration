package uk.gov.hmcts.reform.fpl.validation.validators.time;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.DateNotInFuture;

import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DateNotInFutureValidator implements ConstraintValidator<DateNotInFuture, LocalDateTime> {

    private final Time time;

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext context) {
        return !localDateTime.toLocalDate().isAfter(time.now().toLocalDate());
    }
}
