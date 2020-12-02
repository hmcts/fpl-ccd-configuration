package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;

@Service
public class CaseSubmissionChecker extends CompoundEventChecker {

    @Autowired
    private FeatureToggleService featureToggleService;

    private static final List<Event> REQUIRED_EVENTS = List.of(
            CASE_NAME,
            ORDERS_SOUGHT,
            HEARING_URGENCY,
            GROUNDS,
            DOCUMENTS,
            ORGANISATION_DETAILS,
            CHILDREN,
            RESPONDENTS,
            ALLOCATION_PROPOSAL
    );

    private static final List<Event> REQUIRED_EVENTS_AFTER_APPLICATION_DOCUMENTS_ENABLED = List.of(
        CASE_NAME,
        ORDERS_SOUGHT,
        HEARING_URGENCY,
        GROUNDS,
        ORGANISATION_DETAILS,
        CHILDREN,
        RESPONDENTS,
        ALLOCATION_PROPOSAL
    );

    @Override
    public List<String> validate(CaseData caseData) {
        if (featureToggleService.isApplicationDocumentsEventEnabled()) {
            return super.validate(caseData, REQUIRED_EVENTS_AFTER_APPLICATION_DOCUMENTS_ENABLED);
        }
        return super.validate(caseData, REQUIRED_EVENTS);
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
}
