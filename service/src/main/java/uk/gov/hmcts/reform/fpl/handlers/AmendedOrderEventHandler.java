package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AmendedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_AMENDED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
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
    private final SealedOrderHistoryService sealedOrderHistoryService;
    private final OtherRecipientsInbox otherRecipientsInbox;

    @EventListener
    public void notifyParties(final AmendedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        //final DocumentReference orderDocument = orderEvent.getOrderDocument();
        final DocumentReference orderDocument = DocumentReference.builder().build();
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);

        // TODO type may need changed to AMEND order
        List<Element<Other>> othersSelected = getOthersSelected(lastGeneratedOrder);

        sendNotificationToLocalAuthorityAndDigitalRepresentatives(caseData, orderDocument, othersSelected);
        sendNotificationToEmailServedRepresentatives(caseData, orderDocument, othersSelected);
    }

    @EventListener
    public void sendOrderByPost(final AmendedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final List<DocumentReference> documents = List.of(DocumentReference.builder().build());
        //final List<DocumentReference> documents = List.of(orderEvent.getOrderDocument());
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);

        Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

        if (lastGeneratedOrder.isNewVersion()) {
            List<Element<Other>> othersSelected = getOthersSelected(lastGeneratedOrder);
            allRecipients.removeAll(otherRecipientsInbox.getNonSelectedRecipients(
                POST, caseData, othersSelected, element -> element.getValue()
            ));
            allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(othersSelected));
        }

        System.out.println("I am posting to" + allRecipients);

        sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(allRecipients));
    }

    @SuppressWarnings("unchecked")
    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final DocumentReference orderDocument,
                                                              final List<Element<Other>> othersSelected) {
        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);
        Set<String> digitalRecipientsOtherNotNotified = (Set<String>) otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL, caseData, othersSelected, element -> element.getValue().getEmail()
        );
        emailRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

        System.out.println("I am sending to email reps" + emailRepresentatives);

        if (!emailRepresentatives.isEmpty()) {
            final NotifyData notifyData = amendedOrderEmailContentProvider.getNotifyData(caseData,
                orderDocument, GENERATED_ORDER);

            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                emailRepresentatives,
                ORDER_AMENDED_NOTIFICATION_TEMPLATE
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void sendNotificationToLocalAuthorityAndDigitalRepresentatives(final CaseData caseData,
                                                                           final DocumentReference orderDocument,
                                                                           List<Element<Other>> othersSelected) {
        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE);
        Set<String> digitalRecipientsOtherNotNotified = (Set<String>) otherRecipientsInbox.getNonSelectedRecipients(
            DIGITAL_SERVICE, caseData, othersSelected, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

        final NotifyData notifyData = amendedOrderEmailContentProvider.getNotifyData(caseData,
            orderDocument, GENERATED_ORDER);

        sendToLocalAuthority(caseData, notifyData);

        System.out.println("I am sending to digital reps" + digitalRepresentatives);

        representativeNotificationService.sendNotificationToRepresentatives(
            caseData.getId(),
            notifyData,
            digitalRepresentatives,
            ORDER_AMENDED_NOTIFICATION_TEMPLATE
        );
    }

    private void sendToLocalAuthority(final CaseData caseData,
                                      final NotifyData notifyData) {
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        System.out.println("I am sending to local authority" + emails);

        notificationService.sendEmail(
            ORDER_AMENDED_NOTIFICATION_TEMPLATE, emails, notifyData,
            caseData.getId().toString());
    }

    private List<Element<Other>> getOthersSelected(GeneratedOrder lastGeneratedOrder) {
        List<Element<Other>> othersSelected = lastGeneratedOrder.isNewVersion()
            ? defaultIfNull(lastGeneratedOrder.getOthers(), new ArrayList<>()) : new ArrayList<>();
        return othersSelected;
    }
}
