package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyGatekeeperEventHandler {
    private final NotificationService notificationService;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;

    @EventListener
    public void sendEmailToGatekeeper(NotifyGatekeeperEvent event) {
        EventData eventData = new EventData(event);
        String email = (String) eventData.getCaseDetails().getData().get("gateKeeperEmail");
        Map<String, Object> parameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(
            eventData.getCaseDetails(), eventData.getLocalAuthorityCode());

        notificationService.sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, email, parameters,
            eventData.getReference());
    }
}
