package uk.gov.hmcts.reform.fpl.service.respondent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RespondentValidator {

    private static final int MAX_RESPONDENTS = 10;

    private final RespondentService respondentService;
    private final ValidateEmailService validateEmailService;
    private final RespondentAfterSubmissionValidator respondentAfterSubmissionValidator;
    private final FeatureToggleService featureToggleService;

    private final Time time;

    public List<String> validate(CaseData caseData, CaseData caseDataBefore) {
        List<String> errors = new ArrayList<>();

        validateMaximumSize(caseData).ifPresent(errors::add);

        errors.addAll(validateDob(caseData));

        List<Respondent> respondentsWithLegalRep =
            respondentService.getRespondentsWithLegalRepresentation(caseData.getRespondents1());
        List<String> emails = respondentService.getRespondentSolicitorEmails(respondentsWithLegalRep);
        errors.addAll(validateEmailService.validate(emails, "Representative"));

        if (featureToggleService.hasRSOCaseAccess() && caseData.getState() != OPEN) {
            errors.addAll(respondentAfterSubmissionValidator.validate(caseData, caseDataBefore));
        }

        return errors;
    }

    private List<String> validateDob(CaseData caseData) {
        List<String> dobErrors = new ArrayList<>();
        List<Element<Respondent>> allRespondents = caseData.getAllRespondents();

        for (int i = 0; i < allRespondents.size(); i++) {
            LocalDate dob = allRespondents.get(i).getValue().getParty().getDateOfBirth();
            if (dob != null) {
                if (dob.isAfter(time.now().toLocalDate())) {
                    dobErrors.add(String.format("Date of birth for respondent %s cannot be in the future", i + 1));
                }
            }
        }
        return dobErrors;
    }

    private Optional<String> validateMaximumSize(CaseData caseData) {
        if (caseData.getAllRespondents().size() > 10) {
            return Optional.of(String.format("Maximum number of respondents is %s", MAX_RESPONDENTS));
        }
        return Optional.empty();
    }

}
