package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedCafcassContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.Collection;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatekeepingOrderEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final SDOIssuedCafcassContentProvider cafcassContentProvider;
    private final SDOIssuedContentProvider standardContentProvider;
    private final TranslationRequestService translationRequestService;

    @Async
    @EventListener
    public void notifyCafcass(GatekeepingOrderEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData parameters = cafcassContentProvider.getNotifyData(caseData, event.getOrder());
        String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService.sendEmail(
            event.getNotificationGroup().getCafcassTemplate(), recipient, parameters, caseData.getId()
        );
    }

    @Async
    @EventListener
    public void notifyLocalAuthority(GatekeepingOrderEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = standardContentProvider.buildNotificationParameters(caseData);

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()
        );

        notificationService.sendEmail(
            event.getNotificationGroup().getLaTemplate(), emails, notifyData, caseData.getId().toString()
        );
    }

    @Async
    @EventListener
    public void notifyCTSC(GatekeepingOrderEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = standardContentProvider.buildNotificationParameters(caseData);
        String recipient = ctscEmailLookupConfiguration.getEmail();

        notificationService.sendEmail(
            event.getNotificationGroup().getCtscTemplate(), recipient, notifyData, caseData.getId()
        );
    }

    @Async
    @EventListener
    public void notifyTranslationTeam(GatekeepingOrderEvent event) {
        translationRequestService.sendRequest(event.getCaseData(),
            event.getLanguageTranslationRequirement(),
            event.getOrder(), event.getOrderTitle());
    }
}
