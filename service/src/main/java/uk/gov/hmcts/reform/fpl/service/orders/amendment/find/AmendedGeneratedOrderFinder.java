package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public final class AmendedGeneratedOrderFinder implements AmendedOrderFinder<GeneratedOrder> {

    @Override
    public Optional<GeneratedOrder> findOrderIfPresent(CaseData caseData, CaseData caseDataBefore) {
        List<Element<GeneratedOrder>> currentOrders = new ArrayList<>(caseData.getOrderCollection());
        List<Element<GeneratedOrder>> oldOrders = new ArrayList<>(caseDataBefore.getOrderCollection());

        currentOrders.removeAll(oldOrders);

        return currentOrders.stream().findFirst().map(Element::getValue);
    }
}
