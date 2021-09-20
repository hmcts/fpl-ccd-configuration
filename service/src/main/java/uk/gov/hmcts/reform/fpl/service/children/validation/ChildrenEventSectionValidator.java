package uk.gov.hmcts.reform.fpl.service.children.validation;

import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

public interface ChildrenEventSectionValidator {
    // User messages
    String MAIN_REP_REMOVAL_ERROR = "You cannot remove the main representative from the case";
    String MAIN_REP_MODIFICATION_ERROR = "You cannot change the main representative";
    String CHILD_REMOVAL_ERROR = "You cannot remove %s from the case";
    String CHILD_ADDITION_ERROR = "You cannot add a child to the case";
    String CHILD_REP_ALTERED_ERROR = "You cannot change a child's legal representative";

    // Exception messages
    String SAME_REP_NULL_ERROR = "The field childrenHaveSameRepresentation should not be null";
    String USE_MAIN_REP_NULL_ERROR = "The field useMainSolicitor in childRepresentationDetails%d should not be null";
    String HAVE_REP_NULL_ERROR = "The field childrenHaveRepresentation should not be null";

    boolean accepts(ChildrenEventSection section);

    List<String> validate(CaseData caseData, CaseData caseDataBefore);
}
