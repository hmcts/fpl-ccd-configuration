package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@Service
public class ApplicationDocumentChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        return !isEmpty(caseData.getTemporaryApplicationDocuments());
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
