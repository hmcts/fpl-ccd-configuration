package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.LOCAL_AUTHORITY_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;

@Service
public class CaseSubmissionChecker extends CompoundEventChecker {

    @Autowired
    private FeatureToggleService featureToggles;

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, getRequiredEvents());
    }

    public List<EventValidationErrors> validateAsGroups(CaseData caseData) {
        return super.validateEvents(caseData, getRequiredEvents());
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

    private List<Event> getRequiredEvents() {
        return List.of(
            CASE_NAME,
            ORDERS_SOUGHT,
            HEARING_URGENCY,
            GROUNDS,
            featureToggles.isApplicantAdditionalContactsEnabled() ? LOCAL_AUTHORITY_DETAILS : ORGANISATION_DETAILS,
            CHILDREN,
            RESPONDENTS,
            ALLOCATION_PROPOSAL);
    }
}
