package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2UploadedEventHandler {
    private final IdamApi idamApi;
    private final RequestData requestData;
    private final NotificationService notificationService;
    private final C2UploadedEmailContentProvider c2UploadedEmailContentProvider;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;

    @EventListener
    public void sendEmailForC2Upload(final C2UploadedEvent event) {
        List<String> roles = idamApi.retrieveUserInfo(requestData.authorisation()).getRoles();
        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoles())) {
            EventData eventData = new EventData(event);
            Map<String, Object> parameters = c2UploadedEmailContentProvider.buildC2UploadNotification(
                eventData.getCaseDetails());

            String email = hmctsEmailContentProvider.getHmctsAdminEmail(eventData);

            notificationService.sendEmail(C2_UPLOAD_NOTIFICATION_TEMPLATE, email, parameters,
                eventData.getReference());
        }
    }
}
