package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderReadyForJudgeReviewEventHandler {
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @EventListener
    public void sendEmailForCaseManagementOrderReadyForJudgeReview(
        final CaseManagementOrderReadyForJudgeReviewEvent event) {
        EventData eventData = new EventData(event);

        Map<String, Object> parameters = caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(eventData.getCaseDetails());

        String email = adminNotificationHandler.getHmctsAdminEmail(eventData);

        notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, email, parameters,
            eventData.getReference());
    }
}
