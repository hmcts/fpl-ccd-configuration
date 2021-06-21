package uk.gov.hmcts.reform.fpl.service.children;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ChildRepresentativeSolicitorValidator {

    private static final String FULL_NAME_VALIDATION_MESSAGE = "Add the full name of child %d's legal representative";
    private static final String NO_EMAIL_VALIDATION_MESSAGE = "Add the email address of child %d's legal "
                                                              + "representative";
    private static final String INVALID_EMAIL_FORMAT_VALIDATION_MESSAGE =
        "Enter an email address in the correct format for child %d's legal representative, for example "
        + "name@example.com";
    private static final String ORGANISATION_DETAILS_VALIDATION_MESSAGE = "Add the organisation details for child "
                                                                          + "%d's legal representative";

    private final ValidateEmailService emailValidator;

    public List<String> validateChildRepresentationDetails(CaseData caseData) {
        if (YES.getValue().equals(caseData.getChildrenEventData().getChildrenHaveSameRepresentation())) {
            return List.of();
        }

        return validate(caseData);
    }

    public List<String> validateMainChildRepresentative(CaseData caseData) {
        ChildrenEventData eventData = caseData.getChildrenEventData();

        if (NO.getValue().equals(eventData.getChildrenHaveRepresentation())) {
            return List.of();
        }

        return emailValidator.validate(
            eventData.getChildrenMainRepresentative().getEmail(),
            "Enter an email address in the correct format for child main legal representative, for example "
            + "name@example.com"
        ).map(List::of).orElseGet(List::of);
    }

    private List<String> validate(CaseData caseData) {
        int numOfChildren = caseData.getAllChildren().size();
        ChildrenEventData eventData = caseData.getChildrenEventData();
        List<ChildRepresentationDetails> childrenDetails = eventData.getAllRepresentationDetails();

        List<String> errors = new ArrayList<>();
        for (int i = 0, idx = 1; i < childrenDetails.size(); i++, idx++) {
            // ignore fields that will be null
            if (i == numOfChildren) {
                break;
            }

            ChildRepresentationDetails details = childrenDetails.get(i);

            if (YES.getValue().equals(details.getUseMainSolicitor())) {
                continue;
            }

            RespondentSolicitor representative = details.getSolicitor();

            if (!representative.hasFullName()) {
                errors.add(format(FULL_NAME_VALIDATION_MESSAGE, idx));
            }

            String email = representative.getEmail();
            if (isEmpty(email)) {
                errors.add(format(NO_EMAIL_VALIDATION_MESSAGE, idx));
            } else {
                emailValidator.validate(email, format(INVALID_EMAIL_FORMAT_VALIDATION_MESSAGE, idx))
                    .ifPresent(errors::add);
            }

            if (!representative.hasOrganisationDetails()) {
                errors.add(format(ORGANISATION_DETAILS_VALIDATION_MESSAGE, idx));
            }
        }

        return errors;
    }
}
