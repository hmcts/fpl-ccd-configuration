package uk.gov.hmcts.reform.fpl.service.children.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.CHILD_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.MAIN_REPRESENTATIVE;

@Component
public final class ChildrenEventValidator {
    private static final List<State> RESTRICTED_STATES = List.of(OPEN, RETURNED);

    private final List<ChildrenEventSectionValidator> validators;

    @Autowired
    public ChildrenEventValidator(List<ChildrenEventSectionValidator> validators) {
        this.validators = validators;
    }

    public List<String> validateCollectionUpdates(CaseData caseData, CaseData caseDataBefore) {
        return validate(caseData, caseDataBefore, COLLECTION);
    }

    public List<String> validateMainRepresentativeUpdates(CaseData caseData, CaseData caseDataBefore) {
        return validate(caseData, caseDataBefore, MAIN_REPRESENTATIVE);
    }

    public List<String> validateChildRepresentativeUpdates(CaseData caseData, CaseData caseDataBefore) {
        return validate(caseData, caseDataBefore, CHILD_REPRESENTATIVES);
    }

    private List<String> validate(CaseData caseData, CaseData caseDataBefore, ChildrenEventSection section) {
        if (RESTRICTED_STATES.contains(caseData.getState())) {
            return List.of();
        }

        return validators.stream()
            .filter(validator -> validator.accepts(section))
            .findFirst()
            .map(validator -> validator.validate(caseData, caseDataBefore))
            .orElse(List.of());
    }
}
