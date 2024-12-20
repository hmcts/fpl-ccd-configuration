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
            risks.getWhatKindOfRiskAndHarmToChildren(),
            risks.getFactorsAffectingParenting(),
            risks.getAnythingElseAffectingParenting());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final Risks risks = caseData.getRisks();

        if (risks == null || anyEmpty(
            risks.getWhatKindOfRiskAndHarmToChildren(),
            risks.getFactorsAffectingParenting())) {
            return false;
        }

        return true;
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
