package uk.gov.hmcts.reform.validators;

import uk.gov.hmcts.reform.fpl.model.HearingBookingDetail;
import uk.gov.hmcts.reform.validators.interfaces.HasValidDate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasValidDateValidator implements ConstraintValidator<HasValidDate, HearingBookingDetail> {

    @Override
    public boolean isValid(HearingBookingDetail hearingBookingDetail, ConstraintValidatorContext context) {
        LocalDate todaysDate = LocalDate.now();
        return hearingBookingDetail.getHearingDate().isAfter(todaysDate);
    }
}
