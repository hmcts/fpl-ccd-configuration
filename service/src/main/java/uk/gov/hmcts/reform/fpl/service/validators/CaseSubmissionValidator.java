package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ENTER_CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;

@Service
public class CaseSubmissionValidator extends CompoundEventValidator {

    private static final List<Event> REQUIRED_EVENTS = List.of(
        CASE_NAME,
        ORDERS_NEEDED,
        HEARING_NEEDED,
        GROUNDS,
        DOCUMENTS,
        APPLICANT,
        ENTER_CHILDREN,
        RESPONDENTS,
        ALLOCATION_PROPOSAL
    );

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, REQUIRED_EVENTS);
    }
}
