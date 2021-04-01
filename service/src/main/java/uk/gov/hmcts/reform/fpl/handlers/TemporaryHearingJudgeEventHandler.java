package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.TemporaryHearingJudgeAllocationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.TemporaryHearingJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.TemporaryHearingJudgeContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TemporaryHearingJudgeEventHandler {
    private final NotificationService notificationService;
    private final TemporaryHearingJudgeContentProvider temporaryHearingJudgeContentProvider;

    @EventListener
    public void notifyTemporaryHearingJudge(TemporaryHearingJudgeAllocationEvent event) {
        CaseData caseData = event.getCaseData();
        HearingBooking selectedHearing = event.getSelectedHearing();
        String email = selectedHearing.getJudgeAndLegalAdvisor().getJudgeEmailAddress();

        TemporaryHearingJudgeTemplate parameters = temporaryHearingJudgeContentProvider.buildNotificationParameters(
            caseData, selectedHearing
        );

        notificationService.sendEmail(TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE, email, parameters,
            caseData.getId().toString());
    }
}
