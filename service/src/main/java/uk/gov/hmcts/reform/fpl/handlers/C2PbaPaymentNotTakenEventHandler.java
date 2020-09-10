package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.C2PbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2PbaPaymentNotTakenEventHandler {
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @EventListener
    public void sendEmail(final C2PbaPaymentNotTakenEvent event) {
        final CaseData caseData = event.getCaseData();
        final String email = adminNotificationHandler.getHmctsAdminEmail(caseData);
        final Map<String, Object> parameters = c2UploadedEmailContentProvider
            .buildC2UploadPbaPaymentNotTakenNotification(caseData);

        notificationService.sendEmail(C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, email, parameters,
            caseData.getId().toString());
    }
}
