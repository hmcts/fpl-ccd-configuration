package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.AllocateHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.AllocateHearingJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AllocateHearingJudgeContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_ALLOCATED_TO_HEARING_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocateHearingJudgeEventHandler {
    private final NotificationService notificationService;
    private final AllocateHearingJudgeContentProvider allocatedJudgeContentProvider;

    @EventListener
    public void notifyAllocatedHearingJudge(AllocateHearingJudgeEvent event) {
        CaseData caseData = event.getCaseData();
        HearingBooking selectedHearing = event.getSelectedHearing();
        String email = selectedHearing.getJudgeAndLegalAdvisor().getJudgeEmailAddress();

        AllocateHearingJudgeTemplate parameters = allocatedJudgeContentProvider.buildNotificationParameters(caseData,
            selectedHearing);

        notificationService.sendEmail(JUDGE_ALLOCATED_TO_HEARING_TEMPLATE, email, parameters,
            caseData.getId().toString());
    }
}
