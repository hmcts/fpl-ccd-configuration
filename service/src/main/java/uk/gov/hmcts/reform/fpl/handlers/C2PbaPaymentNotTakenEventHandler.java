package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.C2PbaPaymentNotTakenEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C2PbaPaymentNotTakenEventHandler {
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final C2UploadedEmailContentProvider c2UploadedEmailContentProvider;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void notifyAdmin(final C2PbaPaymentNotTakenEvent event) {
        final CaseData caseData = event.getCaseData();

        final String recipient = adminNotificationHandler.getHmctsAdminEmail(caseData);
        final NotifyData notifyData = c2UploadedEmailContentProvider.getPbaPaymentNotTakenNotifyData(caseData);

        if (featureToggleService.isUploadAdditionalApplicationsEnabled()) {
            notificationService
                .sendEmail(INTERLOCUTORY_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE,
                    recipient,
                    notifyData,
                    caseData.getId());
        } else {
            notificationService
                .sendEmail(C2_UPLOAD_PBA_PAYMENT_NOT_TAKEN_TEMPLATE, recipient, notifyData, caseData.getId());
        }
    }
}
