package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSectionValidator;
import uk.gov.hmcts.reform.fpl.service.children.validation.user.LocalAuthorityUserValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NOT_SPECIFIED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.CHILD_REPRESENTATIVES;

@Component
public final class LocalAuthorityUserIndividualRepresentativeValidator extends LocalAuthorityUserValidator
    implements ChildrenEventSectionValidator {

    private final ChildRepresentativeValidator childRepValidator;

    @Autowired
    public LocalAuthorityUserIndividualRepresentativeValidator(UserService user,
                                                               ChildRepresentativeValidator childRepValidator) {
        super(user);
        this.childRepValidator = childRepValidator;
    }

    @Override
    public boolean accepts(ChildrenEventSection section) {
        return CHILD_REPRESENTATIVES == section && acceptsUser();
    }

    @Override
    public List<String> validate(CaseData caseData, CaseData caseDataBefore) {
        // cannot change once set
        ChildrenEventData currentData = caseData.getChildrenEventData();
        ChildrenEventData oldData = caseDataBefore.getChildrenEventData();

        YesNo oldSameRepresentation = YesNo.fromString(oldData.getChildrenHaveSameRepresentation());
        YesNo currentSameRepresentation = YesNo.fromString(currentData.getChildrenHaveSameRepresentation());

        if (NOT_SPECIFIED == oldSameRepresentation) {
            if (NOT_SPECIFIED == currentSameRepresentation) {
                throw new NullPointerException(SAME_REP_NULL_ERROR);
            }

            if (YES == currentSameRepresentation) {
                return List.of();
            }

            // validate
            return childRepValidator.validate(caseData);
        }

        // now nothing can change
        if (oldSameRepresentation != currentSameRepresentation) {
            return List.of(SAME_REPRESENTATION_ALTERED_ERROR);
        }

        if (YES == currentSameRepresentation) {
            return List.of();
        }

        // make sure that all rep details are the same
        List<String> errors = new ArrayList<>();
        List<Element<Child>> children = caseData.getAllChildren();
        int numChildren = children.size();

        List<ChildRepresentationDetails> currentDetails = currentData.getAllRepresentationDetails();
        List<ChildRepresentationDetails> oldDetails = oldData.getAllRepresentationDetails();

        for (int i = 0; i < numChildren; i++) {
            ChildRepresentationDetails currentDetail = currentDetails.get(i);
            ChildRepresentationDetails oldDetail = oldDetails.get(i);

            if (!Objects.equals(oldDetail, currentDetail)) {
                String childName = children.get(i).getValue().getParty().getFullName();
                errors.add(String.format(CHILD_REP_ALTERED_ERROR, childName));
            }
        }

        return errors;
    }
}
