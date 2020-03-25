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
    private final CaseManagementOrderCaseLinkNotificationHandler caseManagementOrderCaseLinkNotificationHandler;
    private final CaseManagementOrderDocumentLinkNotificationHandler caseManagementOrderDocumentLinkNotificationHandler;

    @EventListener
    public void sendEmailsForIssuedCaseManagementOrder(final CaseManagementOrderIssuedEvent event) {
        EventData eventData = new EventData(event);

        caseManagementOrderCaseLinkNotificationHandler.sendCMOCaseLinkNotifications(eventData);
        caseManagementOrderDocumentLinkNotificationHandler.sendCMODocumentLinkNotifications(eventData,
            event.getDocumentContents());
    }
}
