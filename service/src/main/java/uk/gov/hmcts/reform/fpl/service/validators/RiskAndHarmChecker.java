package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Risks;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class RiskAndHarmChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final Risks risks = caseData.getRisks();

        if (isEmpty(risks)) {
            return false;
        }

        return anyNonEmpty(
            risks.getNeglect(),
            risks.getSexualAbuse(),
            risks.getPhysicalHarm(),
            risks.getEmotionalHarm());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final Risks risks = caseData.getRisks();

        if (risks == null || anyEmpty(
            risks.getNeglect(),
            risks.getSexualAbuse(),
            risks.getPhysicalHarm(),
            risks.getEmotionalHarm())) {
            return false;
        }

        if (risks.getNeglect().equals("Yes") && risks.getNeglectOccurrences().isEmpty()) {
            return false;
        } else if (risks.getSexualAbuse().equals("Yes")
            && isEmpty(risks.getSexualAbuseOccurrences())) {
            return false;
        } else if (risks.getPhysicalHarm().equals("Yes")
            && isEmpty(risks.getPhysicalHarmOccurrences())) {
            return false;
        } else {
            return !risks.getEmotionalHarm().equals("Yes")
                || !isEmpty(risks.getEmotionalHarmOccurrences());
        }
    }
}
