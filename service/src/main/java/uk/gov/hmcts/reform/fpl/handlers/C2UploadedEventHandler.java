package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2UploadedEventHandler {
    private final IdamClient idamClient;
    private final RequestData requestData;
    private final NotificationService notificationService;
    private final CourtService courtService;
    private final C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @EventListener
    public void notifyAdmin(final C2UploadedEvent event) {
        List<String> roles = idamClient.getUserInfo(requestData.authorisation()).getRoles();
        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoleNames())) {
            CaseData caseData = event.getCaseData();

            NotifyData notifyData = c2UploadedEmailContentProvider
                .getNotifyData(caseData, event.getUploadedBundle().getDocument());

            String recipient = courtService.getCourtEmail(caseData);

            notificationService
                .sendEmail(C2_UPLOAD_NOTIFICATION_TEMPLATE, recipient, notifyData, caseData.getId());
        }
    }
}
