package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.order.ManageOrdersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
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

    public ManageOrdersEvent build(CaseData caseData, CaseData caseDataBefore) {
        List<Element<GeneratedOrder>> currentOrders = caseData.getOrderCollection();
        List<Element<GeneratedOrder>> oldOrders = caseDataBefore.getOrderCollection();
        GeneratedOrder lastGeneratedOrder = historyService.lastGeneratedOrder(caseData);

        if (!isAmendedOrder(currentOrders, oldOrders)) {
            return new GeneratedOrderEvent(caseData, lastGeneratedOrder.getDocument());
        }

        AmendedOrderEvent event = finders.stream()
            .map(finder -> finder.findOrderIfPresent(caseData, caseDataBefore))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .map(order -> new AmendedOrderEvent(
                    caseData, order.getDocument(), order.getAmendedOrderType(), order.getSelectedOthers()
            )).orElseThrow();

        return event;
    }

    private boolean isAmendedOrder(List<Element<GeneratedOrder>> orders, List<Element<GeneratedOrder>> ordersBefore) {
        return ordersBefore.size() == orders.size();
    }
}
