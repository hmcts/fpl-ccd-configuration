package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
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

        if (!hearing.getTimeFrame().equals("Within 18 days")
            && isNullOrEmpty(hearing.getReason())) {
            return false;
        } else if (hearing.getWithoutNotice().equals("Yes")
            && isNullOrEmpty(hearing.getWithoutNoticeReason())) {
            return false;
        } else if (hearing.getReducedNotice().equals("Yes")
            && isNullOrEmpty(hearing.getReducedNoticeReason())) {
            return false;
        } else if (hearing.getRespondentsAware().equals("Yes")
            && isNullOrEmpty(hearing.getRespondentsAwareReason())) {
            return false;
        }

        return super.isCompleted(caseData);
    }
}
