package uk.gov.hmcts.reform.fpl.service.respondent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RespondentValidator {

    private static final int MAX_RESPONDENTS = 10;

    private final RespondentService respondentService;
    private final ValidateEmailService validateEmailService;
    private final RespondentAfterSubmissionValidator respondentAfterSubmissionValidator;

    private final Time time;

    public List<String> validate(CaseData caseData, CaseData caseDataBefore) {
        return validate(caseData, caseDataBefore, false);
    }

    public List<String> validate(CaseData caseData, CaseData caseDataBefore, boolean hideRespondentIndex) {
        List<String> errors = new ArrayList<>();

        validateMaximumSize(caseData).ifPresent(errors::add);

        errors.addAll(validateDob(caseData, hideRespondentIndex));
        errors.addAll(validateAddress(caseData));

        List<Respondent> respondentsWithLegalRep =
            respondentService.getRespondentsWithLegalRepresentation(caseData.getRespondents1());
        List<String> emails = respondentService.getRespondentSolicitorEmails(respondentsWithLegalRep);
        errors.addAll(validateEmailService.validate(emails, "Representative"));
        List<String> telephoneNumbers = respondentService.getRespondentSolicitorTelephones(respondentsWithLegalRep);
        if (telephoneNumbers.size() != respondentsWithLegalRep.size()) {
            errors.addAll(List.of("Telephone number of legal representative is required."));
        }

        if (caseData.getState() != OPEN) {
            errors.addAll(respondentAfterSubmissionValidator.validate(caseData, caseDataBefore, hideRespondentIndex));
        }

        return errors;
    }

    private List<String> validateDob(CaseData caseData, boolean hideRespondentIndex) {
        List<String> dobErrors = new ArrayList<>();
        List<Element<Respondent>> allRespondents = caseData.getAllRespondents();

        for (int i = 0; i < allRespondents.size(); i++) {
            LocalDate dob = allRespondents.get(i).getValue().getParty().getDateOfBirth();
            if (dob != null) {
                if (dob.isAfter(time.now().toLocalDate())) {
                    dobErrors.add(String.format("Date of birth for respondent %scannot be in the future",
                        hideRespondentIndex ? "" : ((i + 1) + " ")));
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

    private List<String> validateAddress(CaseData caseData) {
        return caseData.getAllRespondents().stream()
            .map(Element::getValue).map(Respondent::getParty)
            .filter(party -> YesNo.YES.getValue().equals(party.getAddressKnow()))
            .map(Party::getAddress)
            .map(address -> {
                List<String> addErrs = new ArrayList<>();
                if (isEmpty(address)) {
                    addErrs.add("Enter respondent's address");
                } else {
                    if (isBlank(address.getAddressLine1())) {
                        addErrs.add("Building and Street is required");
                    }
                    if (isBlank(address.getPostTown())) {
                        addErrs.add("Town or City is required");
                    }
                    if (isBlank(address.getPostcode())) {
                        addErrs.add("Postcode/Zipcode is required");
                    }
                    if (isBlank(address.getCountry())) {
                        addErrs.add("Country is required");
                    }
                }
                return addErrs;
            }).flatMap(List::stream).collect(Collectors.toList());
    }
}
