package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.FplEvent.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.FplEvent.APPLICANT;
import static uk.gov.hmcts.reform.fpl.FplEvent.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.FplEvent.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.FplEvent.ENTER_CHILDREN;
import static uk.gov.hmcts.reform.fpl.FplEvent.GROUNDS;
import static uk.gov.hmcts.reform.fpl.FplEvent.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.FplEvent.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.FplEvent.RESPONDENTS;

@Service
public class CaseSubmissionValidator extends CompoundEventValidator {

    private static final List<FplEvent> REQUIRED_EVENTS = List.of(
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
