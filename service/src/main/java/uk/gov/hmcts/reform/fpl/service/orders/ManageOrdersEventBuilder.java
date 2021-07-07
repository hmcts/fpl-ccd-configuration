package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.OrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersEventBuilder {
    private final SealedOrderHistoryService historyService;

    public OrderEvent build(CaseData caseData, CaseData caseDataBefore) {
        List<Element<GeneratedOrder>> currentOrders = caseData.getOrderCollection();
        List<Element<GeneratedOrder>> oldOrders = caseDataBefore.getOrderCollection();
        GeneratedOrder lastGeneratedOrder = historyService.lastGeneratedOrder(caseData);

        if (!isAmendedOrder(currentOrders, oldOrders)) {
            System.out.println("I am triggering a generated order event");
            return new GeneratedOrderEvent(caseData, lastGeneratedOrder.getDocument());
        }

        System.out.println("I am triggering an amended order event");
        return new AmendedOrderEvent(caseData, lastGeneratedOrder.getDocument());
    }

    private boolean isAmendedOrder(List<Element<GeneratedOrder>> orders, List<Element<GeneratedOrder>> ordersBefore) {
        return ordersBefore.size() == orders.size();
    }
}
