package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderIssuedEventHandler {
    private final CaseLinkNotificationHandler caseLinkNotificationHandler;
    private final DocumentLinkNotificationHandler documentLinkNotificationHandler;

    @EventListener
    public void sendEmailsForIssuedCaseManagementOrder(final CaseManagementOrderIssuedEvent event) {
        EventData eventData = new EventData(event);

        caseLinkNotificationHandler.sendNotifications(eventData);
        documentLinkNotificationHandler.sendNotifications(eventData,
            event.getDocumentContents());
    }
}
