package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class FactorsAffectingParentingChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final FactorsParenting factors = caseData.getFactorsParenting();

        if (isEmpty(factors)) {
            return false;
        }

        return anyNonEmpty(
            factors.getAlcoholDrugAbuse(),
            factors.getDomesticViolence(),
            factors.getAnythingElse());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final FactorsParenting factors = caseData.getFactorsParenting();

        if (factors == null || anyEmpty(
            factors.getAlcoholDrugAbuse(),
            factors.getDomesticViolence(),
            factors.getAnythingElse())) {
            return false;
        }

        if (factors.getAlcoholDrugAbuse().equals("Yes")
            && isNullOrEmpty(factors.getAlcoholDrugAbuseReason())
        ) {
            return false;
        } else if (factors.getDomesticViolence().equals("Yes")
            && isNullOrEmpty(factors.getDomesticViolenceReason())) {
            return false;
        } else {
            return !factors.getAnythingElse().equals("Yes")
                || !isNullOrEmpty(factors.getAnythingElseReason());
        }
    }
}
