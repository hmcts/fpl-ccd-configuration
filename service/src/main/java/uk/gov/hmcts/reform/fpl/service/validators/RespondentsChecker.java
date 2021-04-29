package uk.gov.hmcts.reform.fpl.service.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.respondent.RespondentAfterSubmissionValidator;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.allEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyAddress;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyEmail;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyTelephone;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RespondentsChecker extends PropertiesChecker {
    private final FeatureToggleService featureToggleService;
    private final RespondentAfterSubmissionValidator respondentAfterSubmissionValidator;

    @Override
    public List<String> validate(CaseData caseData) {

        List<String> errors = new ArrayList<>(respondentAfterSubmissionValidator.validateLegalRepresentation(caseData));
        errors.addAll(super.validate(caseData, List.of("respondents1")));
        return errors;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final List<Respondent> respondents = unwrapElements(caseData.getAllRespondents());

        switch (respondents.size()) {
            case 0:
                return false;
            case 1:
                return !isEmptyRespondent(respondents.get(0), featureToggleService.isRespondentJourneyEnabled());
            default:
                return true;
        }
    }

    private static boolean isEmptyRespondent(Respondent respondent, boolean featureToggle) {

        if (isEmpty(respondent)) {
            return true;
        }

        final RespondentParty respondentParty = respondent.getParty();

        if (isEmpty(respondentParty)) {
            return true;
        }

        List<Object> respondentPartyFields = new ArrayList<>();
        respondentPartyFields.add(respondentParty.getFirstName());
        respondentPartyFields.add(respondentParty.getLastName());
        respondentPartyFields.add(respondentParty.getDateOfBirth());
        respondentPartyFields.add(respondentParty.getGender());
        respondentPartyFields.add(respondentParty.getRelationshipToChild());
        respondentPartyFields.add(respondentParty.getContactDetailsHidden());
        respondentPartyFields.add(respondentParty.getLitigationIssues());

        if (featureToggle) {
            respondentPartyFields.add(respondent.getLegalRepresentation());
        }

        return isEmptyAddress(respondentParty.getAddress())
            && isEmptyTelephone(respondentParty.getTelephoneNumber())
            && isEmptyEmail(respondentParty.getEmail())
            && allEmpty(respondentPartyFields.toArray(new Object[0]));
    }

}
