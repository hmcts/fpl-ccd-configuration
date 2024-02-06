package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class C1WithSupplementChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final SubmittedC1WithSupplementBundle submittedC1WithSupplement = caseData.getSubmittedC1WithSupplement();
        if (isEmpty(submittedC1WithSupplement)) {
            return false;
        }
        return anyNonEmpty(submittedC1WithSupplement.getDocument());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final SubmittedC1WithSupplementBundle submittedC1WithSupplement = caseData.getSubmittedC1WithSupplement();
        return !(submittedC1WithSupplement == null || anyEmpty(submittedC1WithSupplement.getDocument()));
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
