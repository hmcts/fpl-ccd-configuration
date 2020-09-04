package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.CMO;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderReadyForJudgeReviewEventHandler {
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void notifyAdmin(final CaseManagementOrderReadyForJudgeReviewEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseData);
        String recipient = adminNotificationHandler.getHmctsAdminEmail(caseData);

        notificationService
            .sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    @EventListener
    public void notifyAllocatedJudge(
        final CaseManagementOrderReadyForJudgeReviewEvent event) {
        CaseData caseData = event.getCaseData();

        if (featureToggleService.isAllocatedJudgeNotificationEnabled(CMO) && caseData.hasAllocatedJudgeEmail()) {
            NotifyData notifyData = caseManagementOrderEmailContentProvider
                .buildCMOReadyForJudgeReviewNotificationParameters(caseData);
            String recipient = caseData.getAllocatedJudge().getJudgeEmailAddress();

            notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE, recipient,
                notifyData, caseData.getId());
        }
    }
}
