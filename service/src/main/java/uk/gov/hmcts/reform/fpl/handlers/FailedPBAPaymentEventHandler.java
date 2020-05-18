package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FailedPBAPaymentEventHandler {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final FailedPBAPaymentContentProvider failedPBAPaymentContentProvider;

    @EventListener
    public void sendFailedPBAPaymentEmailToLocalAuthority(FailedPBAPaymentEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = failedPBAPaymentContentProvider.buildLANotificationParameters(
            event.getApplicationType());

        String email = inboxLookupService.getNotificationRecipientEmail(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());

        notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA, email, parameters,
            eventData.getReference());
    }

    @EventListener
    public void sendFailedPBAPaymentEmailToCTSC(FailedPBAPaymentEvent event) {
        EventData eventData = new EventData(event);
        Map<String, Object> parameters = failedPBAPaymentContentProvider.buildCtscNotificationParameters(
            eventData.getCaseDetails(), event.getApplicationType());

        String email = ctscEmailLookupConfiguration.getEmail();

        notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC, email, parameters,
            eventData.getReference());
    }
}
