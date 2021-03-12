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
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_APPROVES_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersApprovedEventHandler {

    private final NotificationService notificationService;
    private final ReviewDraftOrdersEmailContentProvider contentProvider;
    private final InboxLookupService inboxLookupService;
    private final RepresentativeNotificationService representativeNotificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final SendDocumentService sendDocumentService;

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
    public void sendNotificationToCafcassAndRepresentatives(final DraftOrdersApproved event) {
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

        List<Representative> emailRepresentatives = caseData.getRepresentativesByServedPreference(EMAIL);

        if (!emailRepresentatives.isEmpty()) {
            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                content,
                emailRepresentatives,
                JUDGE_APPROVES_DRAFT_ORDERS
            );
        }

        List<Representative> digitalRepresentatives = caseData.getRepresentativesByServedPreference(DIGITAL_SERVICE);

        if (!digitalRepresentatives.isEmpty()) {
            content = contentProvider.buildOrdersApprovedContent(caseData, hearing, approvedOrders, DIGITAL_SERVICE);

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
    public void sendDocumentToPostRecipients(final DraftOrdersApproved event) {
        final CaseData caseData = event.getCaseData();

        final List<DocumentReference> documents = event.getApprovedOrders()
            .stream()
            .map(HearingOrder::getOrder)
            .collect(
                Collectors.toList());

        final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);

        sendDocumentService.sendDocuments(caseData, documents, recipients);
    }
}