package uk.gov.hmcts.reform.fpl.validation.validators.time;

import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasStartDateAfterEndDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasStartDateAfterEndDateValidator implements
    ConstraintValidator<HasStartDateAfterEndDate, HearingBooking> {
    @Override
    public boolean isValid(HearingBooking hearingBooking, ConstraintValidatorContext context) {
        return hearingBooking.getEndDate().isAfter(hearingBooking.getStartDate());
    }
}
