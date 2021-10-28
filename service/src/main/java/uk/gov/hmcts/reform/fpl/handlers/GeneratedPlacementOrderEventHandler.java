package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedPlacementOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_TYPES;
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

    @EventListener
    public void sendPlacementOrderEmail(final GeneratedPlacementOrderEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);

        final NotifyData notifyData = orderIssuedEmailContentProvider.getNotifyDataForPlacementOrder(caseData,
            orderEvent.getOrderDocument(), lastGeneratedOrder.getChildren().get(0).getValue());

        sendOrderByEmail(caseData, notifyData);
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

        List<Respondent> parents = childPlacement.getNoticeDocuments().stream()
            .map(Element::getValue)
            .filter(noticeDoc -> PARENT_TYPES.contains(noticeDoc.getType()))
            .map(PlacementNoticeDocument::getRespondentId)
            .map(respondentId -> caseData.findRespondent(respondentId).orElseThrow())
            .map(Element::getValue)
            .collect(Collectors.toList());

        Set<String> emailRecipients = new HashSet<>();
        List<Recipient> postRecipients = new ArrayList<>();
        for (Respondent parent : parents) {
            boolean parentNotRepresented = Objects.isNull(parent.getSolicitor());
            if (parentNotRepresented) {
                postRecipients.add(parent.getParty());
            } else {
                emailRecipients.add(parent.getSolicitor().getEmail());
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

    private void sendOrderByEmail(final CaseData caseData,
                                  final NotifyData notifyData) {

        Set<String> recipients = new HashSet<>();

        //Local authority
        recipients.addAll(
            localAuthorityRecipients.getRecipients(RecipientsRequest.builder().caseData(caseData).build())
        );

        //Admin
        recipients.add(courtService.getCourtEmail(caseData));

        //CAFCASS
        recipients.add(cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail());

        sendEmail(notifyData, recipients, caseData.getId());
    }

    private void sendEmail(NotifyData notifyData, Set<String> recipients, Long caseDataId) {
        notificationService.sendEmail(PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE,
            recipients,
            notifyData,
            caseDataId);
    }

}
