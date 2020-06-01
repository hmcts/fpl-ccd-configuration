package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
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
    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void sendEmailForCaseManagementOrderReadyForJudgeReview(
        final CaseManagementOrderReadyForJudgeReviewEvent event) {
        EventData eventData = new EventData(event);

        AllocatedJudgeTemplateForCMO parameters = caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(eventData.getCaseDetails());

        String email = adminNotificationHandler.getHmctsAdminEmail(eventData);

        notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void sendEmailForCaseManagementOrderReadyForJudgeReviewToAllocatedJudge(
        final CaseManagementOrderReadyForJudgeReviewEvent event) {
        EventData eventData = new EventData(event);
        CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);
        if (featureToggleService.isAllocatedJudgeNotificationEnabled(CMO)) {
            if (caseData.allocatedJudgeExists()) {
                AllocatedJudgeTemplateForCMO parameters = caseManagementOrderEmailContentProvider
                    .buildCMOReadyForJudgeReviewNotificationParameters(eventData.getCaseDetails());

                String email = caseData.getAllocatedJudge().getJudgeEmailAddress();

                notificationService.sendEmail(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE, email, parameters,
                    eventData.getReference());
            }
        }
    }
}
