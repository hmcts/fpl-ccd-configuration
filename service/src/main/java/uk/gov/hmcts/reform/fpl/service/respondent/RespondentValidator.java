package uk.gov.hmcts.reform.fpl.service.respondent;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Objects;
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
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        validateMaximumSize(caseData).ifPresent(errors::add);

        validateDob(caseData).ifPresent(errors::add);

        List<Respondent> respondentsWithLegalRep =
            respondentService.getRespondentsWithLegalRepresentation(caseData.getRespondents1());
        List<String> emails = respondentService.getRespondentSolicitorEmails(respondentsWithLegalRep);
        errors.addAll(validateEmailService.validate(emails, "Representative"));

        if (featureToggleService.hasRSOCaseAccess() && caseData.getState() != OPEN) {
            errors.addAll(respondentAfterSubmissionValidator.validate(caseData, caseDataBefore));
        }

        return errors.build();
    }

    private Optional<String> validateDob(CaseData caseData) {
        return caseData.getAllRespondents().stream()
            .map(Element::getValue)
            .map(Respondent::getParty)
            .map(Party::getDateOfBirth)
            .filter(Objects::nonNull)
            .filter(dob -> dob.isAfter(time.now().toLocalDate()))
            .findAny()
            .map(date -> "Date of birth cannot be in the future");
    }

    private Optional<String> validateMaximumSize(CaseData caseData) {
        if (caseData.getAllRespondents().size() > 10) {
            return Optional.of(String.format("Maximum number of respondents is %s", MAX_RESPONDENTS));
        }
        return Optional.empty();
    }

}
