package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_APPROVES_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersApprovedEventHandler {

    private final CourtService courtService;
    private final NotificationService notificationService;
    private final ReviewDraftOrdersEmailContentProvider contentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final RepresentativesInbox representativesInbox;
    private final RepresentativeNotificationService representativeNotificationService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final SendDocumentService sendDocumentService;
    private final OtherRecipientsInbox otherRecipientsInbox;
    private final TranslationRequestService translationRequestService;

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

        String adminEmail = courtService.getCourtEmail(caseData);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .build();

        final Collection<String> localAuthorityEmails = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            adminEmail,
            content,
            caseData.getId()
        );

        notificationService.sendEmail(
            JUDGE_APPROVES_DRAFT_ORDERS,
            localAuthorityEmails,
            content,
            caseData.getId()
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
            caseData.getId()
        );
    }

    @Async
    @EventListener
    public void sendNotificationToDigitalRepresentatives(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> approvedOrders = event.getApprovedOrders();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        Set<String> digitalRepresentatives = new LinkedHashSet<>(
            representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE));
        Set<String> otherRecipientsNotNotified = otherRecipientsInbox.getNonSelectedRecipients(
            DIGITAL_SERVICE, caseData, approvedOrders.get(0).getSelectedOthers(),
            element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(otherRecipientsNotNotified);

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
    public void sendNotificationToEmailRepresentatives(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> approvedOrders = event.getApprovedOrders();

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        NotifyData content = contentProvider.buildOrdersApprovedContent(caseData, hearing, approvedOrders, EMAIL);

        Set<String> emailRepresentatives = new LinkedHashSet<>(
            representativesInbox.getEmailsByPreference(caseData, EMAIL));
        Set<String> otherRecipientsNotNotified = otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL, caseData, approvedOrders.get(0).getSelectedOthers(), element -> element.getValue().getEmail()
        );
        emailRepresentatives.removeAll(otherRecipientsNotNotified);

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
    public void sendDocumentToPostRecipients(final DraftOrdersApproved event) {
        final CaseData caseData = event.getCaseData();

        final List<DocumentReference> documents = event.getApprovedOrders()
            .stream()
            .filter(order -> order.getNeedTranslation() == YesNo.NO)
            .map(HearingOrder::getOrder)
            .collect(Collectors.toList());

        final List<Recipient> recipients = sendDocumentService.getStandardRecipients(caseData);

        List<Element<Other>> othersSelected = event.getApprovedOrders().get(0).getSelectedOthers();
        Set<Recipient> nonSelectedRecipients = otherRecipientsInbox.getNonSelectedRecipients(
            POST, caseData, othersSelected, Element::getValue
        );
        recipients.removeAll(nonSelectedRecipients);

        recipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(othersSelected));
        sendDocumentService.sendDocuments(caseData, documents, recipients);
    }

    @Async
    @EventListener
    public void notifyTranslationTeam(DraftOrdersApproved event) {

        ObjectUtils.<List<HearingOrder>>defaultIfNull(event.getApprovedOrders(), List.of()).forEach(
            order -> translationRequestService.sendRequest(event.getCaseData(),
                Optional.ofNullable(order.getTranslationRequirements()),
                order.getOrder(),
                String.format("%s - %s", defaultIfEmpty(order.getTitle(), "Blank order (C21)"),
                    formatLocalDateTimeBaseUsingFormat(order.getDateIssued().atStartOfDay(), DATE)
                )
            )
        );
    }

}
