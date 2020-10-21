package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NotifyAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AllocatedJudgeContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ALLOCATED_JUDGE_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyAllocatedJudgeEventHandler {
    private final NotificationService notificationService;
    private final AllocatedJudgeContentProvider allocatedJudgeContentProvider;

    @EventListener
    public void notifyAllocatedJudge(NotifyAllocatedJudgeEvent event) {
        CaseData caseData = event.getCaseData();

        AllocatedJudgeTemplate parameters = allocatedJudgeContentProvider.buildNotificationParameters(caseData);

        String email = caseData.getAllocatedJudge().getJudgeEmailAddress();

        notificationService.sendEmail(
            ALLOCATED_JUDGE_TEMPLATE, email, parameters, caseData.getId().toString()
        );
    }
}
