package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedCafcassContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsOrderIssuedEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final SDOIssuedCafcassContentProvider cafcassContentProvider;
    private final SDOIssuedContentProvider standardContentProvider;

    @EventListener
    public void notifyCafcass(StandardDirectionsOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData parameters = cafcassContentProvider.getNotifyData(caseData);
        String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService.sendEmail(SDO_AND_NOP_ISSUED_CAFCASS, recipient, parameters, caseData.getId());
    }

    @EventListener
    public void notifyLocalAuthority(StandardDirectionsOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = standardContentProvider.buildNotificationParameters(caseData);

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()
        );

        notificationService.sendEmail(SDO_AND_NOP_ISSUED_LA, emails, notifyData, caseData.getId().toString());
    }

    @EventListener
    public void notifyCTSC(StandardDirectionsOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = standardContentProvider.buildNotificationParameters(caseData);
        String recipient = ctscEmailLookupConfiguration.getEmail();

        notificationService.sendEmail(SDO_AND_NOP_ISSUED_CTSC, recipient, notifyData, caseData.getId());
    }

}
