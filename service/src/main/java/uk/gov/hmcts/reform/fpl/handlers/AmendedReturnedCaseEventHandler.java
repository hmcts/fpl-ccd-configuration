package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.AmendedReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AmendedReturnedCaseEventHandler {
    private final NotificationService notificationService;
    private final ReturnedCaseContentProvider returnedCaseContentProvider;
    private final HmctsAdminNotificationHandler adminNotificationHandler;

    @Async
    @EventListener
    public void notifyAdmin(AmendedReturnedCaseEvent event) {
        EventData eventData = new EventData(event);

        final String email = adminNotificationHandler.getHmctsAdminEmail(eventData);

        ReturnedCaseTemplate parameters = returnedCaseContentProvider
            .buildNotificationParameters(eventData.getCaseDetails(), eventData.getLocalAuthorityCode());

        notificationService.sendEmail(AMENDED_APPLICATION_RETURNED_TO_THE_ADMIN, email, parameters,
            eventData.getReference());
    }
}
