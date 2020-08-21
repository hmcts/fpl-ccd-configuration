package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.C2_APPLICATION;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2UploadedEventHandler {
    private final IdamClient idamClient;
    private final RequestData requestData;
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final C2UploadedEmailContentProvider c2UploadedEmailContentProvider;
    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void sendNotifications(final C2UploadedEvent event) {
        List<String> roles = idamClient.getUserInfo(requestData.authorisation()).getRoles();
        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoles())) {
            EventData eventData = new EventData(event);
            C2UploadedTemplate parameters = c2UploadedEmailContentProvider.buildC2UploadNotificationTemplate(
                eventData.getCaseDetails(), event.getUploadedBundle().getDocument());

            String email = adminNotificationHandler.getHmctsAdminEmail(eventData);

            notificationService.sendEmail(C2_UPLOAD_NOTIFICATION_TEMPLATE, email, parameters,
                eventData.getReference());
        }
    }

    @EventListener
    public void sendC2UploadedNotificationToAllocatedJudge(final C2UploadedEvent event) {
        EventData eventData = new EventData(event);
        CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        if (featureToggleService.isAllocatedJudgeNotificationEnabled(C2_APPLICATION)
            && caseData.hasAllocatedJudgeEmail()) {
            AllocatedJudgeTemplateForC2 parameters = c2UploadedEmailContentProvider
                .buildC2UploadNotificationForAllocatedJudge(eventData.getCaseDetails());

            String email = caseData.getAllocatedJudge().getJudgeEmailAddress();

            notificationService.sendEmail(C2_UPLOAD_NOTIFICATION_TEMPLATE_JUDGE, email, parameters,
                eventData.getReference());
        }
    }
}
