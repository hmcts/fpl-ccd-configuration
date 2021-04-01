package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;
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

        return anyNonEmpty(hearingPreferences.getWelsh(),
            hearingPreferences.getInterpreter(),
            hearingPreferences.getIntermediary(),
            hearingPreferences.getDisabilityAssistance(),
            hearingPreferences.getExtraSecurityMeasures(),
            hearingPreferences.getSomethingElse());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final HearingPreferences hearingPreferences = caseData.getHearingPreferences();

        if (hearingPreferences == null || anyEmpty(hearingPreferences.getWelsh(),
            hearingPreferences.getInterpreter(),
            hearingPreferences.getIntermediary(),
            hearingPreferences.getDisabilityAssistance(),
            hearingPreferences.getExtraSecurityMeasures(),
            hearingPreferences.getSomethingElse())) {
            return false;
        }

        if (YES.getValue().equals(hearingPreferences.getWelsh())
            && isEmpty(hearingPreferences.getWelshDetails())) {
            return false;
        }

        if (YES.getValue().equals(hearingPreferences.getInterpreter())
            && isEmpty(hearingPreferences.getInterpreterDetails())) {
            return false;
        }

        if (YES.getValue().equals(hearingPreferences.getIntermediary())
            && isEmpty(hearingPreferences.getIntermediaryDetails())) {
            return false;
        }

        if (YES.getValue().equals(hearingPreferences.getDisabilityAssistance())
            && isEmpty(hearingPreferences.getDisabilityAssistanceDetails())) {
            return false;
        }

        if (YES.getValue().equals(hearingPreferences.getExtraSecurityMeasures())
            && isEmpty(hearingPreferences.getExtraSecurityMeasuresDetails())) {
            return false;
        }
        return NO.getValue().equals(hearingPreferences.getSomethingElse())
            || !isEmpty(hearingPreferences.getSomethingElseDetails());
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
