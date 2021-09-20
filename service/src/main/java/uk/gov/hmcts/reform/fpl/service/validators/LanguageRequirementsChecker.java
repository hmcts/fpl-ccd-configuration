package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@Component
public class LanguageRequirementsChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        Optional<String> languageValue = Optional.ofNullable(caseData.getLanguageRequirement());
        return languageValue.isPresent();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }

}
