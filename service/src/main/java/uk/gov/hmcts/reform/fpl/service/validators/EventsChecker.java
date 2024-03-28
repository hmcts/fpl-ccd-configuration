package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.EnumMap;
import java.util.List;
import javax.annotation.PostConstruct;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.LOCAL_AUTHORITY_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SELECT_COURT;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;

@Service
public class EventsChecker {

    @Autowired
    private CaseNameChecker caseNameChecker;

    @Autowired
    private ChildrenChecker childrenChecker;

    @Autowired
    private RespondentsChecker respondentsChecker;

    @Autowired
    private HearingUrgencyChecker hearingUrgencyChecker;

    @Autowired
    private OrdersSoughtChecker ordersSoughtChecker;

    @Autowired
    private GroundsChecker groundsChecker;

    @Autowired
    private OrganisationDetailsChecker organisationDetailsChecker;

    @Autowired
    private LocalAuthorityDetailsChecker localAuthorityDetailsChecker;

    @Autowired
    private AllocationProposalChecker allocationProposalChecker;

    @Autowired
    @Lazy
    private CaseSubmissionChecker caseSubmissionChecker;

    @Autowired
    private RiskAndHarmChecker riskAndHarmChecker;

    @Autowired
    private CourtSelectionChecker courtChecker;

    @Autowired
    private ProceedingsChecker proceedingsChecker;

    @Autowired
    private InternationalElementChecker internationalElementChecker;

    @Autowired
    private C1WithSupplementChecker c1WithSupplementChecker;

    @Autowired
    private OthersChecker othersChecker;

    @Autowired
    private CourtServiceChecker courtServiceChecker;

    @Autowired
    private LanguageRequirementsChecker languageRequirementsChecker;

    @Autowired
    private FactorsAffectingParentingChecker factorsAffectingParentingChecker;

    @Autowired
    private ApplicationDocumentChecker applicationDocumentChecker;

    private final EnumMap<Event, EventChecker> eventCheckers = new EnumMap<>(Event.class);

    @PostConstruct
    public void init() {
        eventCheckers.put(CASE_NAME, caseNameChecker);
        eventCheckers.put(CHILDREN, childrenChecker);
        eventCheckers.put(RESPONDENTS, respondentsChecker);
        eventCheckers.put(HEARING_URGENCY, hearingUrgencyChecker);
        eventCheckers.put(ORDERS_SOUGHT, ordersSoughtChecker);
        eventCheckers.put(GROUNDS, groundsChecker);
        eventCheckers.put(ORGANISATION_DETAILS, organisationDetailsChecker);
        eventCheckers.put(LOCAL_AUTHORITY_DETAILS, localAuthorityDetailsChecker);
        eventCheckers.put(ALLOCATION_PROPOSAL, allocationProposalChecker);
        eventCheckers.put(RISK_AND_HARM, riskAndHarmChecker);
        eventCheckers.put(FACTORS_AFFECTING_PARENTING, factorsAffectingParentingChecker);
        eventCheckers.put(OTHER_PROCEEDINGS, proceedingsChecker);
        eventCheckers.put(INTERNATIONAL_ELEMENT, internationalElementChecker);
        eventCheckers.put(C1_WITH_SUPPLEMENT, c1WithSupplementChecker);
        eventCheckers.put(OTHERS, othersChecker);
        eventCheckers.put(COURT_SERVICES, courtServiceChecker);
        eventCheckers.put(LANGUAGE_REQUIREMENTS, languageRequirementsChecker);
        eventCheckers.put(SUBMIT_APPLICATION, caseSubmissionChecker);
        eventCheckers.put(SELECT_COURT, courtChecker);
        eventCheckers.put(APPLICATION_DOCUMENTS, applicationDocumentChecker);
    }

    public List<String> validate(Event event, CaseData caseData) {
        return ofNullable(eventCheckers.get(event))
            .map(validator -> validator.validate(caseData))
            .orElse(emptyList());
    }

    public boolean isCompleted(Event event, CaseData caseData) {
        return ofNullable(eventCheckers.get(event))
            .map(validator -> validator.isCompleted(caseData))
            .orElse(false);
    }

    public TaskState completedState(Event event) {
        return ofNullable(eventCheckers.get(event))
            .map(EventChecker::completedState)
            .orElse(NOT_AVAILABLE);
    }

    public boolean isInProgress(Event event, CaseData caseData) {
        return ofNullable(eventCheckers.get(event))
            .map(validator -> validator.isStarted(caseData))
            .orElse(false);
    }

    public boolean isAvailable(Event event, CaseData caseData) {
        return ofNullable(eventCheckers.get(event))
            .map(validator -> validator.isAvailable(caseData))
            .orElse(true);
    }
}
