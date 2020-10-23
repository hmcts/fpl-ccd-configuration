package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public Map<String, Object> populateSelectedOrderFields(List<Element<GeneratedOrder>> orders, UUID id) {
        Map<String, Object> orderData = new HashMap<>();
        orders.stream()
            .filter(o -> id.equals(o.getId()))
            .findFirst()
            .ifPresent(orderElement -> {
                GeneratedOrder order = orderElement.getValue();
                orderData.put("orderToBeRemoved", order.getDocument());
                orderData.put("orderTitleToBeRemoved", order.getTitle());
                orderData.put("orderIssuedDateToBeRemoved", order.getDateOfIssue());
                orderData.put("orderDateToBeRemoved", order.getDate());
            });
        return orderData;
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

    public List<Element<Child>> removeFinalOrderPropertiesFromChildren(CaseData caseData) {
        UUID id = getDynamicListValueCode(caseData.getRemovableOrderList(), mapper);

        Optional<GeneratedOrder> removedOrder = caseData.getOrderFromUUID(id);

        if (removedOrder.isEmpty() || !removedOrder.get().isFinalOrder()) {
            return caseData.getAllChildren();
        }

        List<UUID> removedChildrenIDList = removedOrder.get().getChildrenIDs();

        return caseData.getAllChildren().stream()
            .map(element -> {
                if (removedChildrenIDList.contains(element.getId())) {
                    Child child = element.getValue();

                    child.setFinalOrderIssued(null);
                    child.setFinalOrderIssuedType(null);
                }

                return element;
            })
            .collect(Collectors.toList());
    }
}
