package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public final class MainRepresentativeValidator {
    private static final String MAIN_REP_EMAIL_ERROR = "Enter a correct email address, for example name@example.com";

    private final ValidateEmailService emailValidator;

    public List<String> validate(CaseData caseData) {
        ChildrenEventData eventData = caseData.getChildrenEventData();

        if (NO == YesNo.fromString(eventData.getChildrenHaveRepresentation())) {
            return List.of();
        }

        RespondentSolicitor representative = eventData.getChildrenMainRepresentative();

        return emailValidator.validate(representative.getEmail(), MAIN_REP_EMAIL_ERROR)
            .map(List::of)
            .orElseGet(List::of);
    }
}
