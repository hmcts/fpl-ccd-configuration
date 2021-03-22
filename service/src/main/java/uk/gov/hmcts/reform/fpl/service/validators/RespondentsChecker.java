package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.allEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyAddress;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyEmail;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyTelephone;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class RespondentsChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of("respondents1"));
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final List<Respondent> respondents = unwrapElements(caseData.getAllRespondents());

        switch (respondents.size()) {
            case 0:
                return false;
            case 1:
                return !isEmptyRespondent(respondents.get(0));
            default:
                return true;
        }
    }

    private static boolean isEmptyRespondent(Respondent respondent) {

        if (isEmpty(respondent)) {
            return true;
        }

        final RespondentParty respondentParty = respondent.getParty();

        if (isEmpty(respondentParty)) {
            return true;
        }

        return isEmptyAddress(respondentParty.getAddress())
            && isEmptyTelephone(respondentParty.getTelephoneNumber())
            && isEmptyEmail(respondentParty.getEmail())
            && allEmpty(
            respondentParty.getFirstName(),
            respondentParty.getLastName(),
            respondentParty.getDateOfBirth(),
            respondentParty.getGender(),
            respondentParty.getRelationshipToChild(),
            respondentParty.getContactDetailsHidden(),
            respondentParty.getLitigationIssues(),
            respondent.getLegalRepresentation());
    }

}
