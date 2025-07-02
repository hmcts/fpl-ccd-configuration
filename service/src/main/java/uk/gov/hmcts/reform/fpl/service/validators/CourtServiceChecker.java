package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class CourtServiceChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final HearingPreferences hearingPreferences = caseData.getHearingPreferences();

        if (isEmpty(hearingPreferences)) {
            return false;
        }

        return anyNonEmpty(
            hearingPreferences.getWhichCourtServices(),
            hearingPreferences.getInterpreterDetails(),
            hearingPreferences.getIntermediaryDetails(),
            hearingPreferences.getDisabilityAssistanceDetails(),
            hearingPreferences.getExtraSecurityMeasuresDetails(),
            hearingPreferences.getSomethingElseDetails());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final HearingPreferences hearingPreferences = caseData.getHearingPreferences();

        return !(hearingPreferences == null || isEmpty(hearingPreferences.getWhichCourtServices()));
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
