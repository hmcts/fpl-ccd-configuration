package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection;
import uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSectionValidator;
import uk.gov.hmcts.reform.fpl.service.children.validation.user.LocalAuthorityUserValidator;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeSolicitorSanitizer;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NOT_SPECIFIED;
import static uk.gov.hmcts.reform.fpl.service.children.validation.ChildrenEventSection.MAIN_REPRESENTATIVE;

@Component
public final class LocalAuthorityUserMainRepresentativeValidator extends LocalAuthorityUserValidator
    implements ChildrenEventSectionValidator {

    private static final List<YesNo> UNSET_VALUES = List.of(NO, NOT_SPECIFIED);

    private final MainRepresentativeValidator mainRepValidator;
    private final RepresentativeSolicitorSanitizer sanitizer;

    @Autowired
    public LocalAuthorityUserMainRepresentativeValidator(UserService user,
                                                         MainRepresentativeValidator mainRepValidator,
                                                         RepresentativeSolicitorSanitizer sanitizer) {
        super(user);
        this.mainRepValidator = mainRepValidator;
        this.sanitizer = sanitizer;
    }

    @Override
    public boolean accepts(ChildrenEventSection section) {
        return MAIN_REPRESENTATIVE == section && acceptsUser();
    }

    @Override
    public List<String> validate(CaseData caseData, CaseData caseDataBefore) {
        ChildrenEventData currentData = caseData.getChildrenEventData();
        ChildrenEventData oldData = caseDataBefore.getChildrenEventData();

        YesNo oldRepresentation = YesNo.fromString(oldData.getChildrenHaveRepresentation());
        YesNo currentRepresentation = YesNo.fromString(currentData.getChildrenHaveRepresentation());

        if (UNSET_VALUES.contains(oldRepresentation)) {
            if (NOT_SPECIFIED == currentRepresentation) {
                throw new NullPointerException(HAVE_REP_NULL_ERROR);
            }

            if (NO == currentRepresentation) {
                return List.of();
            }

            return mainRepValidator.validate(caseData);
        }

        // already set so just need to check if the representatives are equal
        // No other checks necessary?
        // Allowing the main rep to be removed/swapped out - the NoC service will handle if there are any roles that
        // need to be cleaned up/orgs to be swapped.
        return List.of();
    }
}
