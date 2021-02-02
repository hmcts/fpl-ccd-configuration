package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
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

        if (hearingPreferences.getWelsh().equals("Yes")
            && isNullOrEmpty(hearingPreferences.getWelshDetails())) {
            return false;
        } else if (hearingPreferences.getInterpreter().equals("Yes")
            && isNullOrEmpty(hearingPreferences.getInterpreterDetails())) {
            return false;
        } else if (hearingPreferences.getIntermediary().equals("Yes")
            && isNullOrEmpty(hearingPreferences.getIntermediaryDetails())) {
            return false;
        } else if (hearingPreferences.getDisabilityAssistance().equals("Yes")
            && isNullOrEmpty(hearingPreferences.getDisabilityAssistanceDetails())) {
            return false;
        } else if (hearingPreferences.getExtraSecurityMeasures().equals("Yes")
            && isNullOrEmpty(hearingPreferences.getExtraSecurityMeasuresDetails())) {
            return false;
        } else {
            return !hearingPreferences.getSomethingElse().equals("Yes")
                || !isNullOrEmpty(hearingPreferences.getSomethingElseDetails());
        }
    }
}
