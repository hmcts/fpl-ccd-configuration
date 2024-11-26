package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.events.ModifiedDocumentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.ModifiedItemEmailContentProviderStrategy;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ModifiedDocumentCommonEventHandler {
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final NotificationService notificationService;
    private final ModifiedItemEmailContentProviderStrategy emailContentProviderStrategy;
    private final RepresentativesInbox representativesInbox;
    private final RepresentativeNotificationService representativeNotificationService;
    private final SendDocumentService sendDocumentService;
    private final OtherRecipientsInbox otherRecipientsInbox;


    public void notifyDigitalRepresentatives(final ModifiedDocumentEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getAmendedDocument();
        final List<Element<Other>> selectedOthers = orderEvent.getSelectedOthers();
        final String orderType = orderEvent.getAmendedOrderType();

        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE);
        Set<String> digitalRecipientsOtherNotNotified = otherRecipientsInbox.getNonSelectedRecipients(
            DIGITAL_SERVICE, caseData, selectedOthers, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

        final NotifyData notifyData = emailContentProviderStrategy.getEmailContentProvider(orderEvent)
            .getProvider()
            .getNotifyData(caseData, orderDocument, orderType);

        if (!digitalRepresentatives.isEmpty() && !ModifiedOrderType
            .STANDARD_DIRECTION_ORDER.getLabel().equals(orderType)) {
            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                digitalRepresentatives,
                emailContentProviderStrategy.getEmailContentProvider(orderEvent).getTemplateKey()
            );
        }
    }

    public void notifyEmailRepresentatives(final ModifiedDocumentEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getAmendedDocument();
        final List<Element<Other>> selectedOthers = orderEvent.getSelectedOthers();
        final String orderType = orderEvent.getAmendedOrderType();

        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);
        Set<String> digitalRecipientsOtherNotNotified = otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL, caseData, selectedOthers, element -> element.getValue().getEmail()
        );
        emailRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

        if (!emailRepresentatives.isEmpty() && !ModifiedOrderType
            .STANDARD_DIRECTION_ORDER.getLabel().equals(orderType)) {

            final NotifyData notifyData = emailContentProviderStrategy.getEmailContentProvider(orderEvent)
                .getProvider()
                .getNotifyData(caseData, orderDocument, orderType);

            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                emailRepresentatives,
                emailContentProviderStrategy.getEmailContentProvider(orderEvent).getTemplateKey()
            );
        }
    }


    public void notifyLocalAuthority(final ModifiedDocumentEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getAmendedDocument();
        final String orderType = orderEvent.getAmendedOrderType();

        final NotifyData notifyData = emailContentProviderStrategy.getEmailContentProvider(orderEvent)
            .getProvider()
            .getNotifyData(caseData, orderDocument, orderType);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(emailContentProviderStrategy.getEmailContentProvider(orderEvent).getTemplateKey(),
            recipients, notifyData, caseData.getId());
    }

    public void sendOrderByPost(final ModifiedDocumentEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final List<DocumentReference> documents = List.of(orderEvent.getAmendedDocument());
        final String orderType = orderEvent.getAmendedOrderType();
        final List<Element<Other>> selectedOthers = orderEvent.getSelectedOthers();

        if (!ModifiedOrderType.STANDARD_DIRECTION_ORDER.getLabel().equals(orderType)) {
            Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

            allRecipients.removeAll(otherRecipientsInbox.getNonSelectedRecipients(
                POST, caseData, selectedOthers, Element::getValue
            ));
            allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(selectedOthers));

            sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(allRecipients));
        }
    }
}
