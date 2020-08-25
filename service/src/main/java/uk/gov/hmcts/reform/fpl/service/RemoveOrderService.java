package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@Service
public class RemoveOrderService {
    public DynamicList getDropDownListOfExistingOrders(List<Element<GeneratedOrder>> orders) {
        return asDynamicList(orders, GeneratedOrder::asLabel);
    }

    public void hideOrder(List<Element<GeneratedOrder>> orders, List<Element<GeneratedOrder>> hiddenOrders,
                          DynamicList removableOrderList, String reason) {
        orders.stream()
            .filter(o -> removableOrderList.getValueCode().equals(o.getId()))
            .findFirst()
            .ifPresent(o -> {
                orders.remove(o);
                o.getValue().setRemovalReason(reason);
                hiddenOrders.add(o);
            });
    }
}
