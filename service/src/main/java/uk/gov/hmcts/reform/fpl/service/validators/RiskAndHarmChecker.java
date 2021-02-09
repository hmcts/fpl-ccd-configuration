package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
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

        if (("Yes").equals(risks.getNeglect())
            && isEmpty(risks.getNeglectOccurrences())) {
            return false;
        } else if (("Yes").equals(risks.getSexualAbuse())
            && isEmpty(risks.getSexualAbuseOccurrences())) {
            return false;
        } else if (("Yes").equals(risks.getPhysicalHarm())
            && isEmpty(risks.getPhysicalHarmOccurrences())) {
            return false;
        } else {
            return ("No").equals(risks.getEmotionalHarm())
                || !isEmpty(risks.getEmotionalHarmOccurrences());
        }
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
