package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.AmendedOrderType;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AmendedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_AMENDED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AmendedOrderEventHandler {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final AmendedOrderEmailContentProvider amendedOrderEmailContentProvider;
    private final RepresentativesInbox representativesInbox;
    private final RepresentativeNotificationService representativeNotificationService;
    private final SendDocumentService sendDocumentService;
    private final OtherRecipientsInbox otherRecipientsInbox;

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyDigitalRepresentatives(final AmendedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getAmendedDocument();
        final List<Element<Other>> selectedOthers = orderEvent.getSelectedOthers();
        final String orderType = orderEvent.getAmendedOrderType();

        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE);
        Set<String> digitalRecipientsOtherNotNotified = (Set<String>) otherRecipientsInbox.getNonSelectedRecipients(
            DIGITAL_SERVICE, caseData, selectedOthers, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

        final NotifyData notifyData = amendedOrderEmailContentProvider.getNotifyData(caseData,
            orderDocument, orderType);

        if (!digitalRepresentatives.isEmpty() & !AmendedOrderType
            .STANDARD_DIRECTION_ORDER.getLabel().equals(orderType)) {
            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                digitalRepresentatives,
                ORDER_AMENDED_NOTIFICATION_TEMPLATE
            );
        }
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyEmailRepresentatives(final AmendedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getAmendedDocument();
        final List<Element<Other>> selectedOthers = orderEvent.getSelectedOthers();
        final String orderType = orderEvent.getAmendedOrderType();

        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);
        Set<String> digitalRecipientsOtherNotNotified = (Set<String>) otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL, caseData, selectedOthers, element -> element.getValue().getEmail()
        );
        emailRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

        if (!emailRepresentatives.isEmpty() && !AmendedOrderType
            .STANDARD_DIRECTION_ORDER.getLabel().equals(orderType)) {
            final NotifyData notifyData = amendedOrderEmailContentProvider.getNotifyData(caseData,
                orderDocument, orderType);

            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                emailRepresentatives,
                ORDER_AMENDED_NOTIFICATION_TEMPLATE
            );
        }
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyLocalAuthority(final AmendedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getAmendedDocument();
        final String orderType = orderEvent.getAmendedOrderType();

        final NotifyData notifyData = amendedOrderEmailContentProvider.getNotifyData(caseData,
            orderDocument, orderType);

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        notificationService.sendEmail(
            ORDER_AMENDED_NOTIFICATION_TEMPLATE, emails, notifyData,
            caseData.getId().toString());
    }

    @Async
    @EventListener
    public void sendOrderByPost(final AmendedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final List<DocumentReference> documents = List.of(orderEvent.getAmendedDocument());
        final String orderType = orderEvent.getAmendedOrderType();
        final List<Element<Other>> selectedOthers = orderEvent.getSelectedOthers();

        if (!AmendedOrderType.STANDARD_DIRECTION_ORDER.getLabel().equals(orderType)) {
            Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

            allRecipients.removeAll(otherRecipientsInbox.getNonSelectedRecipients(POST, caseData, selectedOthers,
                element -> element.getValue()));
            allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(selectedOthers));

            sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(allRecipients));
        }
    }
}
