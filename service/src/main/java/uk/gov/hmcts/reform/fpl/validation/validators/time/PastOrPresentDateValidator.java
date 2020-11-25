package uk.gov.hmcts.reform.fpl.validation.validators.time;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.PastOrPresentDate;

import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PastOrPresentDateValidator implements ConstraintValidator<PastOrPresentDate, LocalDateTime> {

    private final Time time;

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext context) {
        if (localDateTime == null) {
            return true;
        }
        return !localDateTime.toLocalDate().isAfter(time.now().toLocalDate());
    }
}
