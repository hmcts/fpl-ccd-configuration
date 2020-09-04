package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.NewCMOUploaded;
import uk.gov.hmcts.reform.fpl.handlers.HmctsAdminNotificationHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.NewCMOUploadedContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NewCMOUploadedEventHandler {
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final NewCMOUploadedContentProvider contentProvider;

    @Async
    @EventListener
    public void notifyAdmin(final NewCMOUploaded event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = contentProvider.buildTemplate(
            event.getHearing(),
            caseData.getId(),
            caseData.getAllocatedJudge(),
            caseData.getAllRespondents(),
            caseData.getFamilyManCaseNumber()
        );
        String recipient = adminNotificationHandler.getHmctsAdminEmail(caseData);

        notificationService
            .sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyAllocatedJudge(final NewCMOUploaded event) {
        CaseData caseData = event.getCaseData();

        if (caseData.hasAllocatedJudgeEmail()) {
            NotifyData notifyData = contentProvider.buildTemplate(
                event.getHearing(),
                caseData.getId(),
                caseData.getAllocatedJudge(),
                caseData.getAllRespondents(),
                caseData.getFamilyManCaseNumber()
            );
            String recipient = caseData.getAllocatedJudge().getJudgeEmailAddress();

            notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE, recipient,
                notifyData, caseData.getId());
        }
    }
}
