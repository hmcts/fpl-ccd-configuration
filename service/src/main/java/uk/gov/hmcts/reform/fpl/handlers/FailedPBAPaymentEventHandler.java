package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;

import java.util.Collection;

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

        FailedPBANotificationData parameters = notificationContent
            .getLocalAuthorityNotifyData(event.getApplicationType());

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .excludeLegalRepresentatives(true)
                .caseData(caseData)
                .build());

        notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA, emails, parameters,
            caseData.getId().toString());
    }

    @EventListener
    public void notifyCTSC(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();

        FailedPBANotificationData parameters = notificationContent.getCtscNotifyData(caseData,
            event.getApplicationType());

        String email = ctscEmailLookupConfiguration.getEmail();

        notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC, email, parameters,
            caseData.getId());
    }
}
