package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSectionValidator;
import uk.gov.hmcts.reform.fpl.service.children.validation.user.LocalAuthorityUserValidator;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeSolicitorSanitizer;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NOT_SPECIFIED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.fromString;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.CHILD_REPRESENTATIVES;

@Component
public final class LocalAuthorityUserIndividualRepresentativeValidator extends LocalAuthorityUserValidator
    implements ChildrenEventSectionValidator {

    private final ChildRepresentativeValidator childRepValidator;
    private final RepresentativeSolicitorSanitizer sanitizer;

    @Autowired
    public LocalAuthorityUserIndividualRepresentativeValidator(UserService user,
                                                               ChildRepresentativeValidator childRepValidator,
                                                               RepresentativeSolicitorSanitizer sanitizer) {
        super(user);
        this.childRepValidator = childRepValidator;
        this.sanitizer = sanitizer;
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

        YesNo oldSameRepresentation = fromString(oldData.getChildrenHaveSameRepresentation());
        YesNo currentSameRepresentation = fromString(currentData.getChildrenHaveSameRepresentation());

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
            return List.of(CHILD_REP_ALTERED_ERROR);
        }

        if (YES == currentSameRepresentation) {
            return List.of();
        }

        // make sure that all rep details are the same
        int numChildren = caseData.getAllChildren().size();
        List<ChildRepresentationDetails> currentDetails = currentData.getAllRepresentationDetails();
        List<ChildRepresentationDetails> oldDetails = oldData.getAllRepresentationDetails();

        for (int i = 0; i < numChildren; i++) {
            ChildRepresentationDetails currentDetail = currentDetails.get(i);
            ChildRepresentationDetails oldDetail = oldDetails.get(i);

            YesNo currentUseMainSolicitor = fromString(currentDetail.getUseMainSolicitor());
            YesNo oldUsedMainSolicitor = fromString(oldDetail.getUseMainSolicitor());

            if (NOT_SPECIFIED == currentUseMainSolicitor) {
                throw new NullPointerException(String.format(USE_MAIN_REP_NULL_ERROR, i));
            }

            if (YES == oldUsedMainSolicitor) {
                if (YES == currentUseMainSolicitor) {
                    continue;
                } else {
                    return List.of(CHILD_REP_ALTERED_ERROR);
                }
            }

            if (oldUsedMainSolicitor != currentUseMainSolicitor) {
                return List.of(CHILD_REP_ALTERED_ERROR);
            }

            RespondentSolicitor currentSolicitor = sanitizer.sanitize(currentDetail.getSolicitor());
            RespondentSolicitor oldSolicitor = sanitizer.sanitize(oldDetail.getSolicitor());

            if (!Objects.equals(oldSolicitor, currentSolicitor)) {
                return List.of(CHILD_REP_ALTERED_ERROR);
            }
        }

        return List.of();
    }
}
