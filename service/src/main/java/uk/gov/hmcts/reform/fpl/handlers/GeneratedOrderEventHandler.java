package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
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
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderEventHandler {
    private final InboxLookupService inboxLookupService;
    private final RepresentativesInbox representativesInbox;
    private final NotificationService notificationService;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final RepresentativeNotificationService representativeNotificationService;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    private final SendDocumentService sendDocumentService;
    private final SealedOrderHistoryService sealedOrderHistoryService;

    @EventListener
    public void notifyParties(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getOrderDocument();
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);

        issuedOrderAdminNotificationHandler.notifyAdmin(caseData, orderDocument, GENERATED_ORDER);

        if (lastGeneratedOrder.isNewVersion()) {
            List<Element<Other>> othersSelected = getSelectedOthers(lastGeneratedOrder);

            sendNotificationToLocalAuthorityAndDigitalRepresentatives(caseData, orderDocument, othersSelected);
            sendNotificationToEmailServedRepresentatives(caseData, orderDocument, othersSelected);
        } else {
            sendNotificationToLocalAuthorityAndDigitalRepresentatives(caseData, orderDocument);
            sendNotificationToEmailServedRepresentatives(caseData, orderDocument);
        }
    }

    @EventListener
    public void sendOrderByPost(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final List<DocumentReference> documents = List.of(orderEvent.getOrderDocument());
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);
        List<Recipient> allRecipients;

        if (lastGeneratedOrder.isNewVersion()) {
            List<Element<Other>> othersSelected = getSelectedOthers(lastGeneratedOrder);

            final List<Recipient> otherRecipients = sendDocumentService.getSelectedOtherRecipients(caseData,
                othersSelected);
            allRecipients = sendDocumentService.getRecipientsExcludingOthers(caseData);
            allRecipients.addAll(otherRecipients);
        } else {
            allRecipients = sendDocumentService.getStandardRecipients(caseData);
        }

        sendDocumentService.sendDocuments(caseData, documents, allRecipients);
    }

    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final DocumentReference orderDocument) {
        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);

        if (!emailRepresentatives.isEmpty()) {
            final NotifyData notifyData = orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(caseData,
                orderDocument, GENERATED_ORDER);

            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                emailRepresentatives,
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES
            );
        }
    }

    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final DocumentReference orderDocument,
                                                              final List<Element<Other>> othersSelected) {
        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreferenceExcludingOthers(caseData,
            EMAIL);
        Set<String> emailRepresentativesForOthers = representativesInbox.getOtherRepresentativesToBeNotified(
            othersSelected, caseData.getRepresentatives(), EMAIL);

        emailRepresentatives.addAll(emailRepresentativesForOthers);

        if (!emailRepresentatives.isEmpty()) {
            final NotifyData notifyData = orderIssuedEmailContentProvider.getNotifyDataWithoutCaseUrl(caseData,
                orderDocument, GENERATED_ORDER);

            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                emailRepresentatives,
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES
            );
        }
    }

    private void sendNotificationToLocalAuthorityAndDigitalRepresentatives(final CaseData caseData,
                                                                           final DocumentReference orderDocument) {
        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        final NotifyData notifyData = orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData,
            orderDocument, GENERATED_ORDER);

        sendToLocalAuthority(caseData, notifyData);

        representativeNotificationService.sendNotificationToRepresentatives(
            caseData.getId(),
            notifyData,
            digitalRepresentatives,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES
        );
    }

    private void sendNotificationToLocalAuthorityAndDigitalRepresentatives(final CaseData caseData,
                                                                           final DocumentReference orderDocument,
                                                                           List<Element<Other>> othersSelected) {
        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreferenceExcludingOthers(caseData,
            DIGITAL_SERVICE);
        Set<String> digitalRepresentativesForOthers = representativesInbox.getOtherRepresentativesToBeNotified(
            othersSelected, caseData.getRepresentatives(), DIGITAL_SERVICE);
        digitalRepresentatives.addAll(digitalRepresentativesForOthers);

        final NotifyData notifyData = orderIssuedEmailContentProvider.getNotifyDataWithCaseUrl(caseData,
            orderDocument, GENERATED_ORDER);

        sendToLocalAuthority(caseData, notifyData);

        representativeNotificationService.sendNotificationToRepresentatives(
            caseData.getId(),
            notifyData,
            digitalRepresentatives,
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES
        );
    }

    private void sendToLocalAuthority(final CaseData caseData,
                                      final NotifyData notifyData) {
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        notificationService.sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, emails, notifyData,
            caseData.getId().toString());
    }

    private List<Element<Other>> getSelectedOthers(GeneratedOrder lastOrder) {
        if (lastOrder.getOthers().isEmpty()) {
            return Collections.emptyList();
        }
        return lastOrder.getOthers();
    }
}
