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
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.*;

import static java.util.stream.Collectors.toList;
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
    private final OthersService othersService;

    @EventListener
    public void notifyParties(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getOrderDocument();
        final List<Element<Other>> othersSelected = caseData.getOrderCollection().get(0).getValue().getOthers();

        issuedOrderAdminNotificationHandler.notifyAdmin(caseData, orderDocument, GENERATED_ORDER);
        sendNotificationToLocalAuthorityAndDigitalRepresentatives(caseData, orderDocument);

        sendNotificationToEmailServedRepresentatives(caseData, orderDocument, othersSelected);
    }

    @EventListener
    public void sendOrderByPost(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final List<DocumentReference> documents = List.of(orderEvent.getOrderDocument());

        final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);

        sendDocumentService.sendDocuments(caseData, documents, recipients);
    }

    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final DocumentReference orderDocument,
                                                              final List<Element<Other>> othersSelected) {
        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);
        Set<String> representativesToBeNotified = representativesInbox.getEmailsByPreferenceExcludingOthers(caseData, EMAIL);

        System.out.println("Others selected " + othersSelected);

        othersSelected.stream().forEach(other -> {
            if (other.getValue().getRepresentedBy().isEmpty()) {
                //Send by print and post
                System.out.println("I am going to send by print and post");
            } else {
                UUID representativeID = other.getValue().getRepresentedBy().get(0).getValue();

                String representativeEmail = caseData.getRepresentatives().stream()
                    .filter(element -> element.getId().equals(representativeID))
                    .map(Element::getValue)
                    .map(Representative::getEmail)
                    .findFirst()
                    .orElse(null);

                System.out.println("Representative to email is" + representativeEmail);
                representativesToBeNotified.add(representativeEmail);

            }
        });

        System.out.println("I am going to notify" + representativesToBeNotified);


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

    private void sendToLocalAuthority(final CaseData caseData,
                                      final NotifyData notifyData) {
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        notificationService.sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, emails, notifyData,
            caseData.getId().toString());
    }
}
