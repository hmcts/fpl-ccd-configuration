package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsValidHearingEdit;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;

public class IsValidHearingEditValidator implements ConstraintValidator<IsValidHearingEdit, CaseData> {
    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        List<Element<HearingBooking>> futureHearings = caseData.getFutureHearings();
        List<Element<HearingBooking>> pastAndTodayHearings = caseData.getPastAndTodayHearings();
        List<Element<HearingBooking>> futureAndTodayHearing = caseData.getFutureAndTodayHearings();

        HearingOptions hearingOption = caseData.getHearingOption();

        return !isInvalidHearingEdit(hearingOption, futureHearings)
            && !isInvalidHearingAdjournment(hearingOption, pastAndTodayHearings)
            && !isInvalidHearingVacate(hearingOption, futureAndTodayHearing);
    }

    private boolean isInvalidHearingEdit(HearingOptions hearingOption, List<Element<HearingBooking>> futureHearings) {
        return EDIT_HEARING.equals(hearingOption) && isEmpty(futureHearings);
    }

    private boolean isInvalidHearingAdjournment(HearingOptions hearingOption,
                                                List<Element<HearingBooking>> pastAndTodayHearings) {
        return ADJOURN_HEARING.equals(hearingOption) && isEmpty(pastAndTodayHearings);
    }

    private boolean isInvalidHearingVacate(HearingOptions hearingOption,
                                           List<Element<HearingBooking>> futureAndTodayHearing) {
        return VACATE_HEARING.equals(hearingOption) && isEmpty(futureAndTodayHearing);
    }
}
