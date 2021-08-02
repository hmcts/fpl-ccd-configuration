package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSectionValidator;
import uk.gov.hmcts.reform.fpl.service.children.validation.user.AdminUserValidator;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NOT_SPECIFIED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.CHILD_REPRESENTATIVES;

@Component
public final class AdminUserIndividualRepresentativeValidator extends AdminUserValidator
    implements ChildrenEventSectionValidator {

    private final ChildRepresentativeValidator childRepValidator;

    @Autowired
    public AdminUserIndividualRepresentativeValidator(UserService user,
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
        ChildrenEventData currentData = caseData.getChildrenEventData();

        YesNo currentSameRepresentation = YesNo.fromString(currentData.getChildrenHaveSameRepresentation());

        if (NOT_SPECIFIED == currentSameRepresentation) {
            throw new NullPointerException(SAME_REP_NULL_ERROR);
        }

        return YES == currentSameRepresentation
               ? List.of()
               : childRepValidator.validate(caseData);
    }
}
