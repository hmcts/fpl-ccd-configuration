package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
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
        sendNotificationToLocalAuthorityAndDigitalRepresentatives(caseData, orderDocument, othersSelected);

        sendNotificationToEmailServedRepresentatives(caseData, orderDocument, othersSelected);
    }

    @EventListener
    public void sendOrderByPost(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final List<DocumentReference> documents = List.of(orderEvent.getOrderDocument());
        final List<Element<Other>> othersSelected = caseData.getOrderCollection().get(0).getValue().getOthers();

        final List<Recipient> otherRecipients = sendDocumentService.getSelectedOtherRecipients(caseData, othersSelected);
        final List<Recipient> allRecipients = sendDocumentService.getRecipientsExcludingOthers(caseData);
        allRecipients.addAll(otherRecipients);

        sendDocumentService.sendDocuments(caseData, documents, allRecipients);
    }

    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final DocumentReference orderDocument,
                                                              final List<Element<Other>> othersSelected) {
        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreferenceExcludingOthers(caseData, EMAIL);

        emailRepresentatives.addAll(getOthersToBeNotified(othersSelected, caseData.getRepresentatives(), EMAIL));

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
                                                                           final DocumentReference orderDocument,
                                                                           List<Element<Other>> othersSelected) {
        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreferenceExcludingOthers(caseData, DIGITAL_SERVICE);

        digitalRepresentatives.addAll(getOthersToBeNotified(othersSelected, caseData.getRepresentatives(), DIGITAL_SERVICE));

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

    private Set<String> getOthersToBeNotified(List<Element<Other>> othersSelected, List<Element<Representative>> representatives, RepresentativeServingPreferences preferences) {
        Set<String> othersToBeNotified = new HashSet<>();

        othersSelected.stream().forEach(other -> {
            Other otherToBeNotified = other.getValue();
            if (othersService.isRepresented(otherToBeNotified)) {

                otherToBeNotified.getRepresentedBy().stream().forEach(representative -> {
                    String representativeEmail = representatives.stream()
                        .filter(element -> element.getId().equals(representative.getValue()) && element.getValue().getServingPreferences() == preferences)
                        .map(Element::getValue)
                        .map(Representative::getEmail)
                        .findFirst()
                        .orElse("");

                    if (!representativeEmail.isEmpty()) {
                        othersToBeNotified.add(representativeEmail);
                    }
                });
            }
        });

        return othersToBeNotified;
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
