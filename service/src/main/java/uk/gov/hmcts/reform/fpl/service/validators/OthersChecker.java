package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;

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
        if (isEmpty(caseData.getOthersV2())) {
            return false;
        }

        for (Element<Other> other : caseData.getOthersV2()) {
            if (!isEmptyOther(other.getValue())) {
                return true;
            }
        }
        return false;
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
