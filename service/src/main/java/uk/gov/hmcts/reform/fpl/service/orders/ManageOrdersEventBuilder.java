package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersEventBuilder {
    private final SealedOrderHistoryService historyService;

    public Optional<GeneratedOrderEvent> build(CaseData caseData, CaseData caseDataBefore) {
        List<Element<GeneratedOrder>> currentOrders = caseData.getOrderCollection();
        List<Element<GeneratedOrder>> oldOrders = caseDataBefore.getOrderCollection();

        if (!isAmendedOrder(currentOrders, oldOrders)) {
            GeneratedOrder lastGeneratedOrder = historyService.lastGeneratedOrder(caseData);
            return Optional.of(new GeneratedOrderEvent(caseData, lastGeneratedOrder.getDocument()));
        }

        return Optional.empty();
    }

    private boolean isAmendedOrder(List<Element<GeneratedOrder>> orders, List<Element<GeneratedOrder>> ordersBefore) {
        return ordersBefore.size() == orders.size();
    }
}
