package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseManagementOrderIssuedEventHandler {
    private final InboxLookupService inboxLookupService;
    private final RepresentativesInbox representativesInbox;
    private final NotificationService notificationService;
    private final CaseManagementOrderEmailContentProvider contentProvider;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    private final OtherRecipientsInbox otherRecipientsInbox;
    private final SendDocumentService sendDocumentService;
    private final TranslationRequestService translationRequestService;

    @EventListener
    @Async
    public void notifyAdmin(final CaseManagementOrderIssuedEvent event) {
        issuedOrderAdminNotificationHandler.notifyAdmin(event.getCaseData(), event.getCmo().getOrder(), CMO);
    }

    @EventListener
    @Async
    public void notifyLocalAuthority(final CaseManagementOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();
        HearingOrder issuedCmo = event.getCmo();

        final IssuedCMOTemplate notifyData = contentProvider.buildCMOIssuedNotificationParameters(
            caseData, issuedCmo, DIGITAL_SERVICE);

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());
        notificationService.sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, emails, notifyData, caseData.getId().toString()
        );
    }

    @EventListener
    @Async
    public void notifyCafcass(final CaseManagementOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();
        HearingOrder issuedCmo = event.getCmo();

        final IssuedCMOTemplate cafcassParameters = contentProvider.buildCMOIssuedNotificationParameters(
            caseData, issuedCmo, EMAIL);
        final String cafcassEmail = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService.sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, cafcassEmail, cafcassParameters, caseData.getId()
        );
    }

    @EventListener
    @Async
    public void notifyEmailRepresentatives(final CaseManagementOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();
        HearingOrder cmo = event.getCmo();

        Set<String> representatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);
        Set<String> otherRecipientsNotNotified = otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL, caseData, cmo.getSelectedOthers(), element -> element.getValue().getEmail()
        );
        representatives.removeAll(otherRecipientsNotNotified);

        IssuedCMOTemplate notifyData = contentProvider.buildCMOIssuedNotificationParameters(caseData, cmo, EMAIL);
        representatives.forEach(representative -> notificationService.sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, representative, notifyData, caseData.getId()
        ));
    }

    @EventListener
    @Async
    public void notifyDigitalRepresentatives(final CaseManagementOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();
        HearingOrder cmo = event.getCmo();

        Set<String> representatives = representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE);
        Set<String> otherRecipientsNotNotified = otherRecipientsInbox.getNonSelectedRecipients(
            DIGITAL_SERVICE, caseData, cmo.getSelectedOthers(), element -> element.getValue().getEmail()
        );
        representatives.removeAll(otherRecipientsNotNotified);

        IssuedCMOTemplate notifyData = contentProvider.buildCMOIssuedNotificationParameters(
            caseData, cmo, DIGITAL_SERVICE);
        representatives.forEach(representative -> notificationService.sendEmail(
            CMO_ORDER_ISSUED_NOTIFICATION_TEMPLATE, representative, notifyData, caseData.getId()
        ));
    }

    @Async
    @EventListener
    public void sendDocumentToPostRepresentatives(final CaseManagementOrderIssuedEvent event) {
        CaseData caseData = event.getCaseData();
        HearingOrder issuedCmo = event.getCmo();

        Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

        List<Element<Other>> othersSelected = issuedCmo.getSelectedOthers();
        Set<Recipient> nonSelectedRecipients = otherRecipientsInbox.getNonSelectedRecipients(
            POST, caseData, othersSelected, Element::getValue
        );
        allRecipients.removeAll(nonSelectedRecipients);

        allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(othersSelected));
        sendDocumentService.sendDocuments(caseData, List.of(event.getCmo().getOrder()),
            new ArrayList<>(allRecipients));
    }

    @Async
    @EventListener
    public void notifyTranslationTeam(CaseManagementOrderIssuedEvent event) {
        translationRequestService.sendRequest(event.getCaseData(),
            Optional.ofNullable(event.getCmo().getTranslationRequirements()),
            event.getCmo().getOrder(), event.getCmo().asLabel()
        );
    }

}
