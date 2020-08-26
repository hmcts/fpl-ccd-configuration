package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListValueCode;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemoveOrderService {
    private final ObjectMapper mapper;

    public DynamicList buildDynamicListOfOrders(List<Element<GeneratedOrder>> orders, UUID selected) {
        List<Element<GeneratedOrder>> blankOrders = orders.stream()
            .filter(order -> order.getValue().isRemovable())
            .collect(Collectors.toList());

        return asDynamicList(blankOrders, selected, GeneratedOrder::asLabel);
    }

    public DynamicList buildDynamicListOfOrders(List<Element<GeneratedOrder>> orders) {
        return buildDynamicListOfOrders(orders, null);
    }

    public void hideOrder(List<Element<GeneratedOrder>> orders, List<Element<GeneratedOrder>> hiddenOrders,
                          Object removableOrderList, String reason) {
        UUID id = getDynamicListValueCode(removableOrderList, mapper);

        orders.stream()
            .filter(order -> id.equals(order.getId()))
            .findFirst()
            .ifPresent(order -> {
                orders.remove(order);
                order.getValue().setRemovalReason(reason);
                hiddenOrders.add(order);
            });
    }
}
