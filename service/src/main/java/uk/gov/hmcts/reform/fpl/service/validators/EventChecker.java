package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.FplEvent.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.FplEvent.APPLICANT;
import static uk.gov.hmcts.reform.fpl.FplEvent.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.FplEvent.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.FplEvent.ENTER_CHILDREN;
import static uk.gov.hmcts.reform.fpl.FplEvent.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.FplEvent.GROUNDS;
import static uk.gov.hmcts.reform.fpl.FplEvent.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.FplEvent.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.FplEvent.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.FplEvent.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.FplEvent.SUBMIT_APPLICATION;

@Service
public class EventChecker {

    @Autowired
    private CaseNameValidator caseNameValidator;

    @Autowired
    private ChildrenValidator childrenValidator;

    @Autowired
    private RespondentsValidator respondentsValidator;

    @Autowired
    private HearingNeededValidator hearingNeededValidator;

    @Autowired
    private OrdersNeededValidator ordersNeededValidator;

    @Autowired
    private GroundsValidator groundsValidator;

    @Autowired
    private ApplicantValidator applicantValidator;

    @Autowired
    AllocationProposalValidator allocationProposalValidator;

    @Autowired
    private DocumentsValidator documentsValidator;

    @Autowired
    private CaseSubmissionValidator submissionValidator;

    @Autowired
    private RiskAndHarmValidator riskAndHarmValidator;

    @Autowired
    private FactorsAffectingParentingValidator factorsAffectingParentingValidator;

    private final EnumMap<FplEvent, Validator> validators = new EnumMap<>(FplEvent.class);

    private final EnumMap<FplEvent, Validator> guards = new EnumMap<>(FplEvent.class);

    @PostConstruct
    public void init() {
        validators.put(CASE_NAME, caseNameValidator);
        validators.put(ENTER_CHILDREN, childrenValidator);
        validators.put(RESPONDENTS, respondentsValidator);
        validators.put(HEARING_NEEDED, hearingNeededValidator);
        validators.put(ORDERS_NEEDED, ordersNeededValidator);
        validators.put(GROUNDS, groundsValidator);
        validators.put(APPLICANT, applicantValidator);
        validators.put(ALLOCATION_PROPOSAL, allocationProposalValidator);
        validators.put(DOCUMENTS, documentsValidator);
        validators.put(RISK_AND_HARM, riskAndHarmValidator);
        validators.put(FACTORS_AFFECTING_PARENTING, factorsAffectingParentingValidator);
        validators.put(SUBMIT_APPLICATION, submissionValidator);

        guards.put(SUBMIT_APPLICATION, submissionValidator);
    }

    public List<String> validate(FplEvent event, CaseData caseData) {
        return ofNullable(validators.get(event))
            .map(validator -> validator.validate(caseData))
            .orElse(emptyList());
    }

    public boolean isCompleted(FplEvent event, CaseData caseData) {
        return ofNullable(validators.get(event))
            .map(validator -> validator.validate(caseData).isEmpty())
            .orElse(false);
    }

    public boolean isAvailable(FplEvent event, CaseData caseData) {
        return ofNullable(guards.get(event))
            .map(validator -> validator.validate(caseData).isEmpty())
            .orElse(true);
    }
}
