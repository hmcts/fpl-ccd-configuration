package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
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
import java.util.Set;

import static java.util.Set.of;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderEventHandler {
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final RepresentativesInbox representativesInbox;
    private final NotificationService notificationService;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final RepresentativeNotificationService representativeNotificationService;
    private final IssuedOrderAdminNotificationHandler issuedOrderAdminNotificationHandler;
    private final SendDocumentService sendDocumentService;
    private final SealedOrderHistoryService sealedOrderHistoryService;
    private final OtherRecipientsInbox otherRecipientsInbox;
    private final TranslationRequestService translationRequestService;
    private final CafcassNotificationService cafcassNotificationService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final UserService userService;
    private final WorkAllocationTaskService workAllocationTaskService;
    private final JudicialService judicialService;

    @EventListener
    public void notifyParties(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final DocumentReference orderDocument = orderEvent.getOrderDocument();
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);

        issuedOrderAdminNotificationHandler.notifyAdmin(caseData, orderDocument, GENERATED_ORDER);

        List<Element<Other>> othersSelected = getOthersSelected(lastGeneratedOrder);
        sendNotificationToLocalAuthorityAndDigitalRepresentatives(caseData, orderDocument, othersSelected);
        sendNotificationToEmailServedRepresentatives(caseData, orderDocument, othersSelected);

    }

    @EventListener
    public void sendOrderByPost(final GeneratedOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final List<DocumentReference> documents = List.of(orderEvent.getOrderDocument());
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);

        if (lastGeneratedOrder.getNeedTranslation() == YesNo.YES) {
            return;
        }

        Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

        if (lastGeneratedOrder.isNewVersion()) {
            List<Element<Other>> othersSelected = getOthersSelected(lastGeneratedOrder);
            allRecipients.removeAll(otherRecipientsInbox.getNonSelectedRecipients(
                POST, caseData, othersSelected, Element::getValue
            ));
            allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(othersSelected));
        }

        sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(allRecipients));
    }

    @EventListener
    public void notifyTranslationTeam(GeneratedOrderEvent event) {
        translationRequestService.sendRequest(event.getCaseData(),
            event.getLanguageTranslationRequirement(),
            event.getOrderDocument(), event.getOrderTitle());
    }

    @EventListener
    public void notifyCafcass(GeneratedOrderEvent orderEvent) {
        CaseData caseData = orderEvent.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            LocalDateTime hearingStartDate = findElement(caseData.getSelectedHearingId(),
                    caseData.getHearingDetails())
                    .map(Element::getValue)
                    .map(HearingBooking::getStartDate)
                    .orElse(null);

            cafcassNotificationService.sendEmail(caseData,
                    of(orderEvent.getOrderDocument()),
                    ORDER,
                    OrderCafcassData.builder()
                            .documentName(orderEvent.getOrderDocument().getFilename())
                            .hearingDate(hearingStartDate)
                            .orderApprovalDate(orderEvent.getOrderApprovalDate())
                            .build()
            );
        }
    }

    @Async
    @EventListener
    public void createWorkAllocationTask(GeneratedOrderEvent event) {
        if (userService.isJudiciaryUser()) {
            CaseData caseData = event.getCaseData();
            workAllocationTaskService.createWorkAllocationTask(caseData, WorkAllocationTaskType.ORDER_UPLOADED);
        }
    }

    @Async
    @EventListener
    public void cleanupRoles(GeneratedOrderEvent event) {
        // If the case is now closed, we should cleanup any AM roles
        if (State.CLOSED.equals(event.getCaseData().getState())) {
            judicialService.deleteAllRolesOnCase(event.getCaseData().getId());
        }
    }

    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final DocumentReference orderDocument,
                                                              final List<Element<Other>> othersSelected) {
        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);
        Set<String> digitalRecipientsOtherNotNotified = otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL, caseData, othersSelected, element -> element.getValue().getEmail()
        );
        emailRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

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
        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE);
        Set<String> digitalRecipientsOtherNotNotified = otherRecipientsInbox.getNonSelectedRecipients(
            DIGITAL_SERVICE, caseData, othersSelected, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

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

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(
            ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES, recipients, notifyData,
            caseData.getId());
    }

    private List<Element<Other>> getOthersSelected(GeneratedOrder lastGeneratedOrder) {
        return lastGeneratedOrder.isNewVersion()
            ? defaultIfNull(lastGeneratedOrder.getOthers(), new ArrayList<>()) : new ArrayList<>();
    }

}
