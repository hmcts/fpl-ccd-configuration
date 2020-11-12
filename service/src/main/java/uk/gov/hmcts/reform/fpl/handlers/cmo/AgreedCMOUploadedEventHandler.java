package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.AgreedCMOUploaded;
import uk.gov.hmcts.reform.fpl.handlers.HmctsAdminNotificationHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.AgreedCMOUploadedContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AgreedCMOUploadedEventHandler {
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final AgreedCMOUploadedContentProvider contentProvider;

    @Async
    @EventListener
    public void sendNotificationForAdmin(final AgreedCMOUploaded event) {
        CaseData caseData = event.getCaseData();

        CMOReadyToSealTemplate template = contentProvider.buildTemplate(
            event.getHearing(),
            caseData.getId(),
            caseData.getAllocatedJudge(),
            caseData.getAllRespondents(),
            caseData.getFamilyManCaseNumber()
        );

        String email = adminNotificationHandler.getHmctsAdminEmail(caseData);

        notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            email,
            template,
            caseData.getId().toString());
    }

    @Async
    @EventListener
    public void sendNotificationForJudge(final AgreedCMOUploaded event) {
        CaseData caseData = event.getCaseData();
        JudgeAndLegalAdvisor judgeAttendingHearing = event.getHearing().getJudgeAndLegalAdvisor();

        if (judgeAttendingHearing.getJudgeEmailAddress() != null || caseData.hasAllocatedJudgeEmail()) {
            AbstractJudge judge = getJudgeToNotify(judgeAttendingHearing, caseData.getAllocatedJudge());

            CMOReadyToSealTemplate template = contentProvider.buildTemplate(
                event.getHearing(),
                caseData.getId(),
                judge,
                caseData.getAllRespondents(),
                caseData.getFamilyManCaseNumber()
            );

            String email = judge.getJudgeEmailAddress();

            notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE,
                email,
                template,
                caseData.getId().toString());
        }
    }

    private AbstractJudge getJudgeToNotify(JudgeAndLegalAdvisor judgeAttendingHearing, Judge allocatedJudge) {
        return judgeAttendingHearing.getJudgeEmailAddress() != null ? judgeAttendingHearing : allocatedJudge;
    }
}
