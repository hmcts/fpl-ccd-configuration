package uk.gov.hmcts.reform.fpl.service.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.respondent.RespondentAfterSubmissionValidator;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class Respondents3rdPartyChecker extends PropertiesChecker {
    private final RespondentAfterSubmissionValidator respondentAfterSubmissionValidator;

    @Override
    public List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>(respondentAfterSubmissionValidator.validateLegalRepresentation(caseData));

        // We no longer _need_ to have a party in respondents1 if just Respondent LA on case, but if we do then we
        // should validate them
        if (isNotEmpty(caseData.getRespondents1())) {
            caseData.getRespondents1().forEach(respondent -> {
                // only validate those that aren't LAs
                if (YesNo.NO.equals(respondent.getValue().getIsLocalAuthority())) {
                    errors.addAll(super.validate(respondent));
                }
            });
        }

        if (isNotEmpty(caseData.getRespondentLocalAuthority())) {
            if (isEmpty(caseData.getRespondentLocalAuthority().getAddress())) {
                errors.add("Respondent Local Authority address is required");
            }
            if (isEmpty(caseData.getRespondentLocalAuthority().getEmail())) {
                errors.add("Respondent Local Authority email is required");
            }
            if (isEmpty(caseData.getRespondentLocalAuthority().getRepresentativeFirstName())
                || isEmpty(caseData.getRespondentLocalAuthority().getRepresentativeLastName())) {
                errors.add("Respondent Local Authority lawyer is required");
            }
            if (YesNo.YES.equals(caseData.getRespondentLocalAuthority().getUsingOtherOrg())
                && isEmpty(caseData.getRespondentLocalAuthority().getOrganisation())) {
                errors.add("Respondent Local Authority outsourcing organisation is required");
            }
        } else {
            errors.add("Respondent Local Authority details need to be added");
        }

        return errors;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }

}
