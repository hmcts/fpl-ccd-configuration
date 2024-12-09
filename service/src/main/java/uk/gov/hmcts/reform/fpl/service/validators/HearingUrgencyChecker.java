package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
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

        return !isEmpty(hearing)
               && anyNonEmpty(hearing.getHearingUrgencyType(), hearing.getRespondentsAware());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final Hearing hearing = caseData.getHearing();

        if (isEmpty(hearing) || anyEmpty(hearing.getHearingUrgencyType(), hearing.getRespondentsAware())) {
            return false;
        }

        if (NO.equals(hearing.getRespondentsAware()) && isEmpty(hearing.getRespondentsAwareReason())) {
            return false;
        }

        switch (hearing.getHearingUrgencyType()) {
            case SAME_DAY:
            case URGENT:
                if (isEmpty(hearing.getHearingUrgencyDetails()) || isEmpty(hearing.getWithoutNotice())
                    || (YES.equals(hearing.getWithoutNotice()) && isEmpty(hearing.getWithoutNoticeReason()))) {
                    return false;
                }
        }

        return true;
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
