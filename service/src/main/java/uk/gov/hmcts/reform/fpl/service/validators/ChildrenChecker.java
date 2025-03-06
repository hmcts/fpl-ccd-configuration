package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.allEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyAddress;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyTelephone;

@Component
public class ChildrenChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of("children1"));
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        switch (caseData.getAllChildren().size()) {
            case 0:
                return false;
            case 1:
                return !isEmptyChild(caseData.getAllChildren().get(0).getValue());
            default:
                return true;
        }
    }

    private static boolean isEmptyChild(Child child) {
        if (isEmpty(child)) {
            return true;
        }

        final ChildParty childParty = child.getParty();

        if (isEmpty(childParty)) {
            return true;
        }

        return allEmpty(childParty.getFirstName(),
                childParty.getLastName(),
                childParty.getDateOfBirth(),
                childParty.getGender(),
                childParty.getLivingSituation(),
                childParty.getKeyDates(),
                childParty.getCareAndContactPlan(),
                childParty.getAdoption(),
                childParty.getMothersName(),
                childParty.getFathersName(),
                childParty.getSocialWorkerName(),
                childParty.getSocialWorkerEmail(),
                childParty.getAdditionalNeeds(),
                childParty.getSocialWorkerEmail())
                && isEmptyTelephone(childParty.getSocialWorkerTelephoneNumber())
                && isEmptyAddress(childParty.getAddress());
    }

}
