package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.OrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.ManageOrdersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.find.AmendedOrderFinder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersEventBuilder {
    private final SealedOrderHistoryService historyService;
    private final List<AmendedOrderFinder<? extends AmendableOrder>> finders;

    public Optional<ManageOrdersEvent> build(CaseData caseData, CaseData caseDataBefore) {
        List<Element<GeneratedOrder>> currentOrders = caseData.getOrderCollection();
        List<Element<GeneratedOrder>> oldOrders = caseDataBefore.getOrderCollection();
        GeneratedOrder lastGeneratedOrder = historyService.lastGeneratedOrder(caseData);

        if (!isAmendedOrder(currentOrders, oldOrders)) {
            return Optional.of(new GeneratedOrderEvent(caseData, lastGeneratedOrder.getDocument()));
        }

        Optional<? extends AmendableOrder> order = finders.stream()
            .map(finder -> finder.findOrderIfPresent(caseData, caseDataBefore))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

        DocumentReference orderToAmend = order.get().getDocument();
        Object amendedOrderType = order.get().getAmendedOrderType();
        System.out.println("Order to amend is" + orderToAmend + amendedOrderType);

        return order.map(amendableOrder -> new AmendedOrderEvent(caseData, amendableOrder));

    }

    private boolean isAmendedOrder(List<Element<GeneratedOrder>> orders, List<Element<GeneratedOrder>> ordersBefore) {
        return ordersBefore.size() == orders.size();
    }
}
