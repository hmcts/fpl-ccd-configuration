package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.handlers.HmctsAdminNotificationHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_APPROVES_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersApprovedEventHandler {

    private final NotificationService notificationService;
    private final ReviewDraftOrdersEmailContentProvider contentProvider;
    private final InboxLookupService inboxLookupService;
    private final RepresentativesInbox representativesInbox;
    private final RepresentativeNotificationService representativeNotificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final SendDocumentService sendDocumentService;
    private final OtherRecipientsInbox otherRecipientsInbox;
    private final FeatureToggleService toggleService;

    @Async
    @EventListener
    public void sendNotificationToAdminAndLA(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> approvedOrders = event.getApprovedOrders();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        final ApprovedOrdersTemplate content = contentProvider.buildOrdersApprovedContent(caseData, hearing,
            approvedOrders, DIGITAL_SERVICE);

        String adminEmail = adminNotificationHandler.getHmctsAdminEmail(caseData);

        Collection<String> localAuthorityEmails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        notificationService.sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            adminEmail,
            content,
            caseData.getId().toString()
        );

        notificationService.sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            localAuthorityEmails,
            content,
            caseData.getId().toString()
        );
    }

    @Async
    @EventListener
    public void sendNotificationToCafcass(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> approvedOrders = event.getApprovedOrders();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        NotifyData content = contentProvider.buildOrdersApprovedContent(caseData, hearing, approvedOrders, EMAIL);

        final String cafcassEmail = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService.sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            cafcassEmail,
            content,
            caseData.getId().toString()
        );
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void sendNotificationToDigitalRepresentatives(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> approvedOrders = event.getApprovedOrders();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE);
        if (toggleService.isServeOrdersAndDocsToOthersEnabled()) {
            Set<String> otherRecipientsNotNotified = (Set<String>) otherRecipientsInbox.getNonSelectedRecipients(
                DIGITAL_SERVICE, caseData, approvedOrders.get(0).getSelectedOthers(),
                element -> element.getValue().getEmail());
            digitalRepresentatives.removeAll(otherRecipientsNotNotified);
        }

        if (!digitalRepresentatives.isEmpty()) {
            NotifyData content = contentProvider.buildOrdersApprovedContent(
                caseData, hearing, approvedOrders, DIGITAL_SERVICE);

            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                content,
                digitalRepresentatives,
                JUDGE_APPROVES_DRAFT_ORDERS
            );
        }
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void sendNotificationToEmailRepresentatives(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> approvedOrders = event.getApprovedOrders();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        NotifyData content = contentProvider.buildOrdersApprovedContent(caseData, hearing, approvedOrders, EMAIL);

        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);
        if (toggleService.isServeOrdersAndDocsToOthersEnabled()) {
            Set<String> otherRecipientsNotNotified = (Set<String>) otherRecipientsInbox.getNonSelectedRecipients(
                EMAIL, caseData, approvedOrders.get(0).getSelectedOthers(), element -> element.getValue().getEmail());
            emailRepresentatives.removeAll(otherRecipientsNotNotified);
        }

        if (!emailRepresentatives.isEmpty()) {
            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                content,
                emailRepresentatives,
                JUDGE_APPROVES_DRAFT_ORDERS
            );
        }
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void sendDocumentToPostRecipients(final DraftOrdersApproved event) {
        final CaseData caseData = event.getCaseData();

        final List<DocumentReference> documents = event.getApprovedOrders()
            .stream()
            .map(HearingOrder::getOrder)
            .collect(Collectors.toList());

        final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);

        if (toggleService.isServeOrdersAndDocsToOthersEnabled()) {
            List<Element<Other>> othersSelected = event.getApprovedOrders().get(0).getSelectedOthers();
            Set<Recipient> nonSelectedRecipients = (Set<Recipient>) otherRecipientsInbox.getNonSelectedRecipients(
                POST, caseData, othersSelected, Element::getValue);
            recipients.removeAll(nonSelectedRecipients);

            recipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(othersSelected));
        }
        sendDocumentService.sendDocuments(caseData, documents, recipients);
    }
}
