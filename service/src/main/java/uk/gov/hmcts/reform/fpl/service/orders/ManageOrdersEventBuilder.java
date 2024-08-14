package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedPlacementOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.ManageOrdersEvent;
import uk.gov.hmcts.reform.fpl.events.order.NonMolestationOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.FL404A_NON_MOLESTATION_ORDER;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersEventBuilder {
    private final SealedOrderHistoryService historyService;
    private final List<AmendedOrderFinder<? extends AmendableOrder>> finders;

    public ManageOrdersEvent build(CaseData caseData, CaseData caseDataBefore, ManageOrdersEventData eventData) {
        List<Element<GeneratedOrder>> currentOrders = caseData.getOrderCollection();
        List<Element<GeneratedOrder>> oldOrders = caseDataBefore.getOrderCollection();

        if (!isAmendedOrder(currentOrders, oldOrders)) {
            GeneratedOrder lastGeneratedOrder = historyService.lastGeneratedOrder(caseData);

            if (A70_PLACEMENT_ORDER.name().equals(lastGeneratedOrder.getOrderType())) {
                return new GeneratedPlacementOrderEvent(caseData,
                    lastGeneratedOrder.getDocument(),
                    lastGeneratedOrder.getNotificationDocument());
            } else if (FL404A_NON_MOLESTATION_ORDER.name().equals(lastGeneratedOrder.getOrderType())) {
                return new NonMolestationOrderEvent(caseData,
                    eventData,
                    lastGeneratedOrder.asLabel(),
                    lastGeneratedOrder.getDocument(),
                    lastGeneratedOrder.getTranslationRequirements()
                );
            } else {
                return new GeneratedOrderEvent(
                    caseData,
                    lastGeneratedOrder.getDocument(),
                    lastGeneratedOrder.getTranslationRequirements(),
                    lastGeneratedOrder.asLabel(),
                    lastGeneratedOrder.getApprovalDate()
                );
            }
        }

        return finders.stream()
            .map(finder -> finder.findOrderIfPresent(caseData, caseDataBefore))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .map(order -> new AmendedOrderEvent(
                    caseData, order.getDocument(), order.getModifiedItemType(), order.getSelectedOthers()
            )).orElseThrow();
    }

    private boolean isAmendedOrder(List<Element<GeneratedOrder>> orders, List<Element<GeneratedOrder>> ordersBefore) {
        return ordersBefore.size() == orders.size();
    }
}
