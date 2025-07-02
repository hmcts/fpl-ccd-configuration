package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedPlacementOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Set.of;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratedPlacementOrderEventHandler {

    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final NotificationService notificationService;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final SealedOrderHistoryService sealedOrderHistoryService;
    private final CourtService courtService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final SendDocumentService sendDocumentService;
    private final CafcassNotificationService cafcassNotificationService;
    private final UserService userService;
    private final WorkAllocationTaskService workAllocationTaskService;
    private final JudicialService judicialService;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void sendPlacementOrderEmail(final GeneratedPlacementOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);

        final NotifyData notifyData = orderIssuedEmailContentProvider.getNotifyDataForPlacementOrder(caseData,
            orderEvent.getOrderDocument(), lastGeneratedOrder.getChildren().get(0).getValue());

        sendOrderByEmail(caseData, notifyData);
    }

    @EventListener
    public void sendPlacementOrderEmailToCafcassEngland(final GeneratedPlacementOrderEvent orderEvent) {
        CaseData caseData = orderEvent.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            cafcassNotificationService.sendEmail(caseData,
                of(orderEvent.getOrderDocument(), orderEvent.getOrderNotificationDocument()),
                ORDER,
                OrderCafcassData.builder()
                    .documentName(orderEvent.getOrderDocument().getFilename())
                    .build()
            );
        }
    }

    @EventListener
    public void sendPlacementOrderNotification(final GeneratedPlacementOrderEvent orderEvent) {
        CaseData caseData = orderEvent.getCaseData();
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);
        UUID childId = lastGeneratedOrder.getChildren().get(0).getId();
        Child child = findElement(childId, caseData.getAllChildren()).map(Element::getValue).orElseThrow();

        Placement childPlacement = caseData.getPlacementEventData().getPlacements().stream()
            .filter(placementElement -> childId.equals(placementElement.getValue().getChildId()))
            .findFirst()
            .map(Element::getValue)
            .orElseThrow();

        List<Respondent> respondentsToNotify = childPlacement.getPlacementRespondentsToNotify().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        Set<String> emailRecipients = new HashSet<>();
        List<Recipient> postRecipients = new ArrayList<>();
        for (Respondent respondent : respondentsToNotify) {
            boolean respondentNotRepresented = Objects.isNull(respondent.getSolicitor());
            if (respondentNotRepresented && !respondent.isDeceasedOrNFA()) {
                postRecipients.add(respondent.getParty());
            } else {
                emailRecipients.add(respondent.getSolicitor().getEmail());
            }
        }

        //Post letters
        sendDocumentService.sendDocuments(caseData, List.of(orderEvent.getOrderNotificationDocument()), postRecipients);

        //E-mail child solicitor - if present
        Optional.ofNullable(child.getSolicitor())
            .map(RespondentSolicitor::getEmail)
            .ifPresent(emailRecipients::add);

        final NotifyData notifyData = orderIssuedEmailContentProvider.getNotifyDataForPlacementOrder(caseData,
            orderEvent.getOrderNotificationDocument(),
            child);
        sendEmail(notifyData, emailRecipients, caseData.getId());
    }

    @Async
    @EventListener
    public void createWorkAllocationTask(GeneratedPlacementOrderEvent event) {
        if (userService.isJudiciaryUser()) {
            CaseData caseData = event.getCaseData();
            workAllocationTaskService.createWorkAllocationTask(caseData, WorkAllocationTaskType.ORDER_UPLOADED);
        }
    }

    @Async
    @EventListener
    public void cleanupRoles(GeneratedPlacementOrderEvent event) {
        // If the case is now closed, we should cleanup any AM roles
        if (State.CLOSED.equals(event.getCaseData().getState())) {
            judicialService.deleteAllRolesOnCase(event.getCaseData().getId());
        }
    }

    private void sendOrderByEmail(final CaseData caseData,
                                  final NotifyData notifyData) {

        Set<String> recipients = new HashSet<>();

        //Local authority
        recipients.addAll(
            localAuthorityRecipients.getRecipients(RecipientsRequest.builder().caseData(caseData).build())
        );

        //Admin
        if (featureToggleService.isWATaskEmailsEnabled()) {
            recipients.add(courtService.getCourtEmail(caseData));
        } else {
            log.info("WA EMAIL SKIPPED - placement order generated - {}", caseData.getId());
        }

        //CAFCASS (WALES ONLY)
        if (CafcassHelper.isNotifyingCafcassWelsh(caseData, cafcassLookupConfiguration)) {
            Optional<String> recipientIsWelsh =
                cafcassLookupConfiguration.getCafcassWelsh(caseData.getCaseLocalAuthority())
                    .map(CafcassLookupConfiguration.Cafcass::getEmail);
            if (recipientIsWelsh.isPresent()) {
                log.info(String.format("Added cafcass (wales) email to recipients for template: {}",
                    PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE));
                recipients.add(recipientIsWelsh.get());
            }
        }
        sendEmail(notifyData, recipients, caseData.getId());
    }

    private void sendEmail(NotifyData notifyData, Set<String> recipients, Long caseDataId) {
        notificationService.sendEmail(PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE,
            recipients,
            notifyData,
            caseDataId);
    }

}
