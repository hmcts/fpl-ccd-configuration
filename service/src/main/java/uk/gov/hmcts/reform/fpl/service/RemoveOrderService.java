package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemoveOrderService {

    private final OrderRemovalActions orderRemovalActions;

    @SuppressWarnings("unchecked")
    public DynamicList buildDynamicListOfOrders(CaseData caseData, UUID selected) {
        List<Element<RemovableOrder>> blankOrders = getRemovableOrderList(caseData).stream()
            .filter(order -> order.getValue().isRemovable())
            .map(order -> (Element<RemovableOrder>) order)
            .collect(Collectors.toList());

        return asDynamicList(blankOrders, selected, RemovableOrder::asLabel);
    }

    public DynamicList buildDynamicListOfOrders(CaseData caseData) {
        return buildDynamicListOfOrders(caseData, null);
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

    public void removeOrderFromCase(CaseData caseData,
                                    Map<String, Object> data,
                                    UUID removedOrderId,
                                    RemovableOrder removableOrder) {
        orderRemovalActions
            .getActions()
            .stream()
            .filter(orderRemovalAction -> orderRemovalAction.isAccepted(removableOrder))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                format("Action not found for order %s", removedOrderId)))
            .action(caseData, data, removedOrderId, removableOrder);
    }

    public RemovableOrder getRemovedOrderByUUID(CaseData caseData, UUID removedOrderId) {
        return getRemovableOrderList(caseData).stream()
            .filter(orderElement -> removedOrderId.equals(orderElement.getId()))
            .map(Element::getValue)
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Failed to find order matching id " + removedOrderId));
    }

    private List<Element<? extends RemovableOrder>> getRemovableOrderList(CaseData caseData) {
        List<Element<? extends RemovableOrder>> orders = new ArrayList<>();
        orders.addAll(caseData.getOrderCollection());
        orders.addAll(caseData.getSealedCMOs());
        orders.addAll(caseData.getStandardDirectionOrder().getDirections());
        return orders;
    }
}
