package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.model.ApproveOrderUrgencyOption;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_APPROVES_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDGE_APPROVES_URGENT_DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
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
    private final CafcassNotificationService cafcassNotificationService;
    private final WorkAllocationTaskService workAllocationTaskService;
    private final FeatureToggleService featureToggleService;

    private boolean isUrgent(CaseData caseData) {
        return Optional.ofNullable(caseData.getOrderReviewUrgency()).orElse(
            ApproveOrderUrgencyOption.builder().urgency(List.of()).build()).getUrgency().contains(YesNo.YES);
    }

    private String getJudgeApprovesDraftOrderTemplateId(CaseData caseData) {
        return isUrgent(caseData) ? JUDGE_APPROVES_URGENT_DRAFT_ORDERS : JUDGE_APPROVES_DRAFT_ORDERS;
    }

    @Async
    @EventListener
    public void sendNotificationToAdmin(final DraftOrdersApproved event) {
        if (featureToggleService.isWATaskEmailsEnabled()) {
            CaseData caseData = event.getCaseData();
            List<HearingOrder> approvedOrders = new ArrayList<>();
            approvedOrders.addAll(event.getApprovedOrders());
            approvedOrders.addAll(unwrapElements(event.getApprovedConfidentialOrders()));

            final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
                caseData.getHearingDetails())
                .map(Element::getValue)
                .orElse(null);

            final ApprovedOrdersTemplate content = contentProvider.buildOrdersApprovedContent(caseData, hearing,
                approvedOrders, DIGITAL_SERVICE);

            String adminEmail = courtService.getCourtEmail(caseData);

            notificationService.sendEmail(
                getJudgeApprovesDraftOrderTemplateId(caseData),
                adminEmail,
                content,
                caseData.getId()
            );
        } else {
            log.info("WA EMAIL SKIPPED - draft order approved - {}", event.getCaseData().getId());
        }
    }

    @Async
    @EventListener
    public void sendNotificationToLA(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> approvedOrders = new ArrayList<>();
        approvedOrders.addAll(event.getApprovedOrders());
        approvedOrders.addAll(event.getApprovedConfidentialOrders().stream()
            .filter(order -> findElement(order.getId(),
                caseData.getConfidentialOrders().getOrderCollectionLA()).isPresent())
            .map(Element::getValue)
            .toList());

        if (approvedOrders.isEmpty()) {
            log.info("Only confidential orders uploaded and not accessible by LA. Skip notifying LA");
            return;
        }

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        final ApprovedOrdersTemplate content = contentProvider.buildOrdersApprovedContent(caseData, hearing,
            approvedOrders, DIGITAL_SERVICE);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .build();

        final Collection<String> localAuthorityEmails = localAuthorityRecipients.getRecipients(recipientsRequest);

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

        if (CafcassHelper.isNotifyingCafcassWelsh(caseData, cafcassLookupConfiguration)) {
            final Optional<Cafcass> recipientIsWelsh =
                cafcassLookupConfiguration.getCafcassWelsh(caseData.getCaseLocalAuthority());
            if (recipientIsWelsh.isPresent()) {
                List<HearingOrder> approvedOrders = new ArrayList<>();
                approvedOrders.addAll(event.getApprovedOrders());
                approvedOrders.addAll(getConfidentialOrderToBeSentToCafcass(event));

                if (approvedOrders.isEmpty()) {
                    log.info("Only confidential orders uploaded and not accessible by Cafcass. "
                             + "Skip notifying Cafcass Welsh");
                    return;
                }

                final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
                    caseData.getHearingDetails())
                    .map(Element::getValue)
                    .orElse(null);

                NotifyData content = contentProvider.buildOrdersApprovedContent(caseData, hearing, approvedOrders,
                    DIGITAL_SERVICE);

                notificationService.sendEmail(
                    JUDGE_APPROVES_DRAFT_ORDERS,
                    recipientIsWelsh.get().getEmail(),
                    content,
                    caseData.getId()
                );
            }
        }
    }

    @Async
    @EventListener
    public void sendNotificationToCafcassViaSendGrid(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            List<HearingOrder> approvedOrders = new ArrayList<>();
            approvedOrders.addAll(event.getApprovedOrders());
            approvedOrders.addAll(getConfidentialOrderToBeSentToCafcass(event));

            if (approvedOrders.isEmpty()) {
                log.info("Only confidential orders uploaded and not accessible by Cafcass. "
                         + "Skip notifying Cafcass England");
                return;
            }

            LocalDateTime hearingStartDate = findElement(caseData.getLastHearingOrderDraftsHearingId(),
                    caseData.getHearingDetails())
                    .map(Element::getValue)
                    .map(HearingBooking::getStartDate)
                    .orElse(null);

            approvedOrders.forEach(hearingOrder ->
                    cafcassNotificationService.sendEmail(caseData,
                            Set.of(hearingOrder.getOrderOrOrderConfidential()),
                            ORDER,
                            OrderCafcassData.builder()
                                    .documentName(hearingOrder.getTitle())
                                    .hearingDate(hearingStartDate)
                                    .orderApprovalDate(hearingOrder.getDateIssued())
                                    .build()
                    ));
        }
    }

    private List<HearingOrder> getConfidentialOrderToBeSentToCafcass(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<Element<GeneratedOrder>> allChildConfidentialOrders = caseData.getConfidentialOrders()
            .getAllChildConfidentialOrders();
        return event.getApprovedConfidentialOrders().stream()
            .filter(order -> findElement(order.getId(), allChildConfidentialOrders).isPresent())
            .map(Element::getValue)
            .toList();
    }

    @Async
    @EventListener
    public void sendNotificationToDigitalRepresentatives(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        List<HearingOrder> approvedOrders = event.getApprovedOrders();
        if (event.getApprovedOrders().isEmpty()) {
            log.info("No non-confidential approved orders. skip sendNotificationToDigitalRepresentatives");
            return;
        }

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
        if (event.getApprovedOrders().isEmpty()) {
            log.info("No non-confidential approved orders. skip sendNotificationToEmailRepresentatives");
            return;
        }

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
    public void sendNotificationToUploaderWhenConfidentialOrderIsApproved(final DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        String adminEmail = courtService.getCourtEmail(caseData);
        Map<String, List<HearingOrder>> confidentiaOrdersMap = event.getApprovedConfidentialOrders()
            .stream()
            .map(Element::getValue)
            .filter(hearingOrder -> adminEmail == null || !adminEmail.equals(hearingOrder.getUploaderEmail()))
            .collect(groupingBy(HearingOrder::getUploaderEmail));

        final HearingBooking hearing = findElement(caseData.getLastHearingOrderDraftsHearingId(),
            caseData.getHearingDetails())
            .map(Element::getValue)
            .orElse(null);

        confidentiaOrdersMap.forEach((uploaderEmail, confidentialOrders) -> {
            NotifyData content = contentProvider.buildOrdersApprovedContent(caseData, hearing, confidentialOrders,
                DIGITAL_SERVICE);
            notificationService.sendEmail(JUDGE_APPROVES_DRAFT_ORDERS, uploaderEmail, content,
                caseData.getId());
        });
    }

    @Async
    @EventListener
    public void sendDocumentToPostRecipients(final DraftOrdersApproved event) {
        final CaseData caseData = event.getCaseData();
        if (event.getApprovedOrders().isEmpty()) {
            log.info("No non-confidential approved orders. skip sendDocumentToPostRecipients");
            return;
        }
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
        List<HearingOrder> approvedOrders = Stream.of(event.getApprovedOrders(),
            unwrapElements(event.getApprovedConfidentialOrders()))
            .flatMap(List::stream)
            .toList();

        approvedOrders.forEach(
            order -> translationRequestService.sendRequest(event.getCaseData(),
                Optional.ofNullable(order.getTranslationRequirements()),
                order.getOrderOrOrderConfidential(),
                String.format("%s - %s", defaultIfEmpty(order.getTitle(), "Blank order (C21)"),
                    formatLocalDateTimeBaseUsingFormat(order.getDateIssued().atStartOfDay(), DATE)
                )
            )
        );
    }

    @Async
    @EventListener
    public void createWorkAllocationTask(DraftOrdersApproved event) {
        CaseData caseData = event.getCaseData();
        workAllocationTaskService.createWorkAllocationTask(caseData, WorkAllocationTaskType.CMO_REVIEWED);
    }
}
