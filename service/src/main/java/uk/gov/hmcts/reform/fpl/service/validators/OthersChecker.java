package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.allEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyAddress;

@Component
public class OthersChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        switch (caseData.getOthersV2().size()) {
            case 0:
                return false;
            case 1:
                return !isEmptyOther(caseData.getOthersV2().get(0).getValue());
            default:
                return true;
        }
    }

    private static boolean isEmptyOther(Other other) {
        if (isEmpty(other)) {
            return true;
        }

        return isEmptyAddress(other.getAddress()) && allEmpty(
                other.getFirstName(),
                other.getLastName(),
                other.getDateOfBirth(),
                other.getChildInformation(),
                other.getLitigationIssues(),
                other.getTelephone(),
                other.getAddressKnowV2(),
                other.getAddressNotKnowReason(),
                other.getHideAddress(),
                other.getHideTelephone());
    }

}
