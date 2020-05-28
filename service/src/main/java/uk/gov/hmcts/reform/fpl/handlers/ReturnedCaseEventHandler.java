package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_RETURNED_TO_THE_LA;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnedCaseEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final ReturnedCaseContentProvider returnedCaseContentProvider;

    @EventListener
    public void notifyLocalAuthority(ReturnedCaseEvent event) {
        EventData eventData = new EventData(event);

        ReturnedCaseTemplate parameters = returnedCaseContentProvider
            .buildNotificationParameters(eventData.getCaseDetails(), eventData.getLocalAuthorityCode());

        String email = inboxLookupService
            .getNotificationRecipientEmail(eventData.getCaseDetails(), eventData.getLocalAuthorityCode());

        notificationService.sendEmail(APPLICATION_RETURNED_TO_THE_LA, email, parameters, eventData.getReference());
    }
}
