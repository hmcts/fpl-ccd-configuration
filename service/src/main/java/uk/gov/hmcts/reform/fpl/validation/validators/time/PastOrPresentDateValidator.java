package uk.gov.hmcts.reform.fpl.validation.validators.time;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.PastOrPresentDate;

import java.time.LocalDateTime;

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
