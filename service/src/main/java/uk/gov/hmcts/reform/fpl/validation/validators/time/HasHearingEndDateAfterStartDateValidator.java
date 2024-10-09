package uk.gov.hmcts.reform.fpl.validation.validators.time;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasHearingEndDateAfterStartDate;


public class HasHearingEndDateAfterStartDateValidator implements
    ConstraintValidator<HasHearingEndDateAfterStartDate, CaseData> {

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext context) {
        if (caseData.getHearingStartDate() != null && caseData.getHearingEndDateTime() != null) {
            return caseData.getHearingEndDateTime().isAfter(caseData.getHearingStartDate());
        }
        return true;
    }
}
