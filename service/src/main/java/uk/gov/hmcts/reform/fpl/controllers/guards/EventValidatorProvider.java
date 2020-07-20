package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
public class EventValidatorProvider {

    @Autowired
    private Validator validator;

    @Autowired
    private CaseSubmissionValidator submissionValidator;

    private final EnumMap<FplEvent, EventValidator> validators = new EnumMap<>(FplEvent.class);
    private final EnumMap<FplEvent, EventValidator> guards = new EnumMap<>(FplEvent.class);

    @PostConstruct
    public void init() {
        validators.put(CASE_NAME, propertyValidator("caseName"));
        validators.put(ENTER_CHILDREN, propertyValidator("children1"));
        validators.put(RESPONDENTS, propertyValidator("respondents1"));
        validators.put(HEARING_NEEDED, propertyValidator("hearing"));
        validators.put(ORDERS_NEEDED, propertyValidator("orders"));
        validators.put(GROUNDS, propertyValidator("grounds"));
        validators.put(APPLICANT, propertyValidator("applicants"));
        validators.put(ALLOCATION_PROPOSAL, propertyValidator("allocationProposal"));
        validators.put(DOCUMENTS, propertyValidator("documents_socialWorkOther",
            "documents_socialWorkCarePlan_document",
            "socialWorkStatementDocument",
            "socialWorkAssessmentDocument",
            "socialWorkChronologyDocument",
            "checklistDocument",
            "thresholdDocument",
            "socialWorkEvidenceTemplateDocument"
        ));

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

    private EventValidator propertyValidator(String... propertiesToBeValidated) {
        return caseData -> validateProperty(caseData, List.of(propertiesToBeValidated));
    }

    private List<String> validateProperty(CaseData caseData, List<String> propertiesToBeValidated) {
        return validator.validate(caseData).stream()
            .filter(violation -> propertiesToBeValidated.contains(getViolatedProperty(violation)))
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
    }

    private String getViolatedProperty(ConstraintViolation violation) {
        final Iterator<Path.Node> paths = violation.getPropertyPath().iterator();
        if (paths.hasNext()) {
            return paths.next().getName();
        }
        return null;
    }

}
