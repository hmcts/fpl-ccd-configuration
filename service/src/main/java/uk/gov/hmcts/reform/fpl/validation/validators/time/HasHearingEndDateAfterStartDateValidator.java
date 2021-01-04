package uk.gov.hmcts.reform.fpl.validation.validators.time;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasHearingEndDateAfterStartDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasHearingEndDateAfterStartDateValidator implements
    ConstraintValidator<HasHearingEndDateAfterStartDate, CaseData> {

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext context) {
        if (caseData.getHearingStartDate() != null && caseData.getHearingEndDate() != null) {
            return caseData.getHearingEndDate().isAfter(caseData.getHearingStartDate());
        }
        return true;
    }
}
