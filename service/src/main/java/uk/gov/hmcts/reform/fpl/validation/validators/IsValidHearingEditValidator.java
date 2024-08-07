package uk.gov.hmcts.reform.fpl.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsValidHearingEdit;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_FUTURE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_PAST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;

public class IsValidHearingEditValidator implements ConstraintValidator<IsValidHearingEdit, CaseData> {
    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        return !isInvalidHearingEditFuture(caseData)
            && !isInvalidHearingEditPast(caseData)
            && !isInvalidHearingAdjournment(caseData)
            && !isInvalidHearingVacate(caseData);
    }

    private boolean isInvalidHearingEditPast(CaseData caseData) {
        return EDIT_PAST_HEARING.equals(caseData.getHearingOption())
            && isEmpty(caseData.getPastHearings());
    }

    private boolean isInvalidHearingEditFuture(CaseData caseData) {
        return EDIT_FUTURE_HEARING.equals(caseData.getHearingOption())
            && isEmpty(caseData.getFutureHearings());
    }

    private boolean isInvalidHearingAdjournment(CaseData caseData) {
        return ADJOURN_HEARING.equals(caseData.getHearingOption()) && isEmpty(caseData.getPastAndTodayHearings());
    }

    private boolean isInvalidHearingVacate(CaseData caseData) {
        return VACATE_HEARING.equals(caseData.getHearingOption()) && isEmpty(caseData.getAllNonCancelledHearings());
    }
}
