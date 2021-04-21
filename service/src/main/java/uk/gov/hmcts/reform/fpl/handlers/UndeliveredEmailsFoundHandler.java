package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.UndeliveredEmailsFound;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.UndeliveredEmailsContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNDELIVERED_EMAILS_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UndeliveredEmailsFoundHandler {
    private final CtscEmailLookupConfiguration ctscEmail;
    private final NotificationService notificationService;
    private final UndeliveredEmailsContentProvider contentProvider;

    @EventListener
    public void sendUndeliveredEmailsReport(UndeliveredEmailsFound event) {
        String recipient = ctscEmail.getEmail();
        NotifyData notifyData = contentProvider.buildParameters(event.getUndeliveredEmails());

        notificationService.sendEmail(UNDELIVERED_EMAILS_TEMPLATE, recipient, notifyData, "undeliveredEmails");
    }
}
