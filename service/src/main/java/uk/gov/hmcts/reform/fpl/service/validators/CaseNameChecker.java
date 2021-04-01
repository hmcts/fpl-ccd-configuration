package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@Component
public class CaseNameChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of("caseName"));
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return isNotEmpty(caseData.getCaseName());
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
