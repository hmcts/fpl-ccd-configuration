package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType.SAME_DAY;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType.URGENT;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class HearingUrgencyChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        List<String> errMsg = super.validate(caseData, List.of("hearing"));
        if (isEmpty(errMsg)) {
            // check if legacy hearing urgency exist but not the latest one
            return isCompleted(caseData) ? List.of() : List.of("Complete the hearing urgency details");
        } else {
            return errMsg;
        }
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

        if (NO.getValue().equalsIgnoreCase(hearing.getRespondentsAware())
            && isEmpty(hearing.getRespondentsAwareReason())) {
            return false;
        }

        if (URGENT.equals(hearing.getHearingUrgencyType()) || SAME_DAY.equals(hearing.getHearingUrgencyType())) {
            if (isEmpty(hearing.getHearingUrgencyDetails()) || isEmpty(hearing.getWithoutNotice())) {
                return false;
            }
            if (YES.getValue().equalsIgnoreCase(hearing.getWithoutNotice())
                && isEmpty(hearing.getWithoutNoticeReason())) {
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
