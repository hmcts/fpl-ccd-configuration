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

            String recipient = hmctsCourtToCourtAdminLookupConfiguration.getEmail(caseData.getCourt().getCode());
            notificationService
                .sendEmail(COURT_ADMIN_LISTING_TEMPLATE, recipient, notifyData, caseData.getId());
        }
    }
}
