package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtToCourtAdminLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.ListAdminEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_ADMIN_LISTING_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_ADMIN_UDO_LISTING_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.SDO;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ListAdminEventHandler {
    private final HmctsCourtToCourtAdminLookupConfiguration hmctsCourtToCourtAdminLookupConfiguration;
    private final SDOIssuedContentProvider standardContentProvider;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void notifyCourtAdmin(ListAdminEvent event) {
        if (event.isSentToAdmin()) {
            CaseData caseData = event.getCaseData();
            NotifyData notifyData = standardContentProvider.buildNotificationParameters(caseData,
                event.getOrder(),
                event.getSendToAdminReason());

            String template = SDO.equals(event.getDirectionsOrderType())
                ? COURT_ADMIN_LISTING_TEMPLATE : COURT_ADMIN_UDO_LISTING_TEMPLATE;

            String recipient = hmctsCourtToCourtAdminLookupConfiguration.getEmail(caseData.getCourt().getCode());
            notificationService.sendEmail(template, recipient, notifyData, caseData.getId());
        }
    }
}
