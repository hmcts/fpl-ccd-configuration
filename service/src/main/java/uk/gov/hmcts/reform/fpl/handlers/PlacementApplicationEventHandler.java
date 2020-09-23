package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementApplicationEventHandler {
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final PlacementApplicationContentProvider placementApplicationContentProvider;

    @EventListener
    public void notifyAdminOfPlacementApplicationUpload(PlacementApplicationEvent event) {
        CaseData caseData = event.getCaseData();
        Map<String, Object> parameters = placementApplicationContentProvider
            .buildPlacementApplicationNotificationParameters(caseData);

        String email = adminNotificationHandler.getHmctsAdminEmail(caseData);

        notificationService.sendEmail(PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, email, parameters,
            caseData.getId().toString());
    }
}
