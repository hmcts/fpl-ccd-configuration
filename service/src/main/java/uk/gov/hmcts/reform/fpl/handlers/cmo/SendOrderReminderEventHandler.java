package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.SendOrderReminderEvent;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ChaseMissingCMOEmailContentProvider;

import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CHASE_OUTSTANDING_ORDER_LA_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SendOrderReminderEventHandler {

    private final NotificationService notificationService;
    private final ChaseMissingCMOEmailContentProvider contentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;

    @EventListener
    public void sendNotificationToApplicant(final SendOrderReminderEvent event) {
        NotifyData notifyData = contentProvider.buildTemplate(event.getCaseData());

        Set<String> recipients = localAuthorityRecipients.getRecipients(RecipientsRequest.builder()
                .caseData(event.getCaseData())
                .build());

        notificationService.sendEmail(CHASE_OUTSTANDING_ORDER_LA_TEMPLATE, recipients, notifyData,
            event.getCaseData().getId());
    }
}
