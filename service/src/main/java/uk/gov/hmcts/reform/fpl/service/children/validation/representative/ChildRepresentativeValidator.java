package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public final class ChildRepresentativeValidator {

    private static final String NO_DETAILS_ERROR = "Confirm %s’s legal representation";
    private static final String FULL_NAME_ERROR = "Add the full name of %s's legal representative";
    private static final String NO_EMAIL_ERROR = "Add the email address of %s's legal representative";
    private static final String ORG_DETAILS_ERROR = "Add the organisation details for %s's legal representative";
    private static final String CHILD_REP_EMAIL_ERROR = "Enter an email address in the correct format for %s's legal "
                                                        + "representative, for example name@example.com";

    private final ValidateEmailService emailValidator;

    public List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        ChildrenEventData eventData = caseData.getChildrenEventData();

        List<Element<Child>> children = caseData.getAllChildren();
        int numOfChildren = children.size();
        List<ChildRepresentationDetails> childrenDetails = eventData.getAllRepresentationDetails();

        for (int i = 0, idx = 1; i < numOfChildren; i++, idx++) {
            ChildRepresentationDetails details = childrenDetails.get(i);
            String childName = children.get(i).getValue().getParty().getFullName();

            if (null == details) {
                errors.add(format(NO_DETAILS_ERROR, childName));
                continue;
            }

            if (YES == YesNo.fromString(details.getUseMainSolicitor())) {
                continue;
            }

            RespondentSolicitor representative = details.getSolicitor();

            if (!representative.hasFullName()) {
                errors.add(format(FULL_NAME_ERROR, childName));
            }

            String email = representative.getEmail();
            if (isEmpty(email)) {
                errors.add(format(NO_EMAIL_ERROR, childName));
            } else {
                emailValidator.validate(email, format(CHILD_REP_EMAIL_ERROR, childName))
                    .ifPresent(errors::add);
            }

            if (!representative.hasOrganisationDetails()) {
                errors.add(format(ORG_DETAILS_ERROR, childName));
            }
        }

        return errors;
    }
}
