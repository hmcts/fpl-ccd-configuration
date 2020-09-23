package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class HearingUrgencyChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of("hearing"));
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final Hearing hearing = caseData.getHearing();

        if (isEmpty(hearing)) {
            return false;
        }

        return anyNonEmpty(
                hearing.getTimeFrame(),
                hearing.getType(),
                hearing.getWithoutNotice(),
                hearing.getReducedNotice(),
                hearing.getRespondentsAware());
    }

}
