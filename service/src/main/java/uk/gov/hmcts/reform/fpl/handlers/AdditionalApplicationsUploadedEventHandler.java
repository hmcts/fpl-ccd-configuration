package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdditionalApplicationsUploadedEventHandler {
    private final RequestData requestData;
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    @EventListener
    public void notifyAdmin(final AdditionalApplicationsUploadedEvent event) {
        List<String> roles = new ArrayList<>(requestData.userRoles());
        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoleNames())) {
            CaseData caseData = event.getCaseData();

            NotifyData notifyData = additionalApplicationsUploadedEmailContentProvider
                .getNotifyData(caseData);
            String recipient = adminNotificationHandler.getHmctsAdminEmail(caseData);
            notificationService
                .sendEmail(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE, recipient, notifyData, caseData.getId());
        }
    }
}
