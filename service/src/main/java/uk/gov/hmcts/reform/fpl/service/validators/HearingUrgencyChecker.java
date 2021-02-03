package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;
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

    @Override
    public boolean isCompleted(CaseData caseData) {
        final Hearing hearing = caseData.getHearing();

        if (hearing == null || anyEmpty(
            hearing.getTimeFrame(),
            hearing.getType(),
            hearing.getTypeGiveReason(),
            hearing.getWithoutNotice(),
            hearing.getReducedNotice(),
            hearing.getRespondentsAware())) {
            return false;
        }

        if (!("Within 18 days").equals(hearing.getTimeFrame())
            && isEmpty(hearing.getReason())) {
            return false;
        } else if (("Yes").equals(hearing.getWithoutNotice())
            && isEmpty(hearing.getWithoutNoticeReason())) {
            return false;
        } else if (("Yes").equals(hearing.getReducedNotice())
            && isEmpty(hearing.getReducedNoticeReason())) {
            return false;
        } else {
            return ("No").equals(hearing.getRespondentsAware())
                || !isEmpty(hearing.getRespondentsAwareReason());
        }
    }
}
