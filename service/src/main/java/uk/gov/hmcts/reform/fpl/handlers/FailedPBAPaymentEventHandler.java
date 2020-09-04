package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FailedPBAPaymentEventHandler {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final FailedPBAPaymentContentProvider notificationContent;

    @EventListener
    public void notifyLocalAuthority(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = notificationContent.getLocalAuthorityNotifyData(event.getApplicationType());
        String recipient = inboxLookupService.getNotificationRecipientEmail(caseData);

        notificationService
            .sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA, recipient, notifyData, caseData.getId());
    }

    @EventListener
    public void notifyCTSC(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = notificationContent.getCtscNotifyData(caseData, event.getApplicationType());
        String recipient = ctscEmailLookupConfiguration.getEmail();

        notificationService
            .sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC, recipient, notifyData, caseData.getId());
    }
}
