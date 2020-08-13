package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingPreferences;

import java.util.List;

import static java.util.Collections.emptyList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.isEmpty;
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
        return false;
    }
}
