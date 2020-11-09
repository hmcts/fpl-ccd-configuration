package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsValidHearingEdit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;

public class IsValidHearingEditValidator implements ConstraintValidator<IsValidHearingEdit, CaseData> {
    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        return !isInvalidHearingEdit(caseData)
            && !isInvalidHearingAdjournment(caseData)
            && !isInvalidHearingVacate(caseData);
    }

    private boolean isInvalidHearingEdit(CaseData caseData) {
        return EDIT_HEARING.equals(caseData.getHearingOption()) && isEmpty(caseData.getFutureHearings());
    }

    private boolean isInvalidHearingAdjournment(CaseData caseData) {
        return ADJOURN_HEARING.equals(caseData.getHearingOption()) && isEmpty(caseData.getPastAndTodayHearings());
    }

    private boolean isInvalidHearingVacate(CaseData caseData) {
        return VACATE_HEARING.equals(caseData.getHearingOption()) && isEmpty(caseData.getFutureAndTodayHearings());
    }
}
