package uk.gov.hmcts.reform.fpl.validation.validators.time;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.EPOTimeRange;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;

import java.time.LocalDateTime;

@Component
public class EPOTimeRangeValidator implements ConstraintValidator<EPOTimeRange, CaseData> {

    private TimeDifference maxDate;

    @Override
    public void initialize(EPOTimeRange annotation) {
        maxDate = annotation.maxDate();
    }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext context) {
        if (caseData.getDateAndTimeOfIssue() == null || caseData.getEpoEndDate() == null) {
            return true;
        }

        LocalDateTime rangeEnd = caseData.getDateAndTimeOfIssue().plus(maxDate.amount(), maxDate.unit());

        return !caseData.getEpoEndDate().isAfter(rangeEnd);
    }
}
