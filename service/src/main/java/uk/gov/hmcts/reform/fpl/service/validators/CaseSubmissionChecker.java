package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_LA;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.SELECT_COURT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Service
public class CaseSubmissionChecker extends CompoundEventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, getRequiredEvents(caseData));
    }

    public List<EventValidationErrors> validateAsGroups(CaseData caseData) {
        return super.validateEvents(caseData, getRequiredEvents(caseData));
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return isNotEmpty(caseData.getDateSubmitted());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        return isNotEmpty(caseData.getDateSubmitted());
    }

    @Override
    public boolean isAvailable(CaseData caseData) {
        return validate(caseData).isEmpty();
    }

    private List<Event> getRequiredEvents(CaseData caseData) {

        final List<Event> events = new ArrayList<>();

        events.add(CASE_NAME);
        events.add(ORDERS_SOUGHT);
        if (!caseData.isC1Application()
            || caseData.isRefuseContactWithChildApplication()
            || caseData.isContactWithChildInCareApplication()) {
            events.add(HEARING_URGENCY); // present but optional for C1s
        }

        if (!caseData.isDischargeOfCareApplication()) {
            events.add(GROUNDS);
        }

        if (YES.equals(caseData.getIsLocalAuthority())) {
            events.add(APPLICANT_DETAILS_LA);
        } else {
            events.add(APPLICANT_DETAILS_SOLICITOR);
        }

        events.add(CHILDREN);
        events.add(RESPONDENTS);
        events.add(ALLOCATION_PROPOSAL);

        if (YES.equals(caseData.getMultiCourts())) {
            events.add(SELECT_COURT);
        }

        return events;
    }

}
