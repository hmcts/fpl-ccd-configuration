package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementApplicationEventHandler {

    private final CourtService courtService;
    private final NotificationService notificationService;
    private final PlacementApplicationContentProvider placementApplicationContentProvider;

    @EventListener
    public void notifyAdmin(PlacementApplicationEvent event) {
        CaseData caseData = event.getCaseData();

        BaseCaseNotifyData notifyData = placementApplicationContentProvider
            .buildPlacementApplicationNotificationParameters(caseData);
        String recipient = courtService.getCourtEmail(caseData);

        notificationService
            .sendEmail(PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, recipient, notifyData, caseData.getId());
    }
}
