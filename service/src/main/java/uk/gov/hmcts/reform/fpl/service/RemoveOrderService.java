package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemoveOrderService {
    private final ObjectMapper mapper;

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

    public List<Element<RemovableOrder>> hideOrder(List<? extends Element<? extends RemovableOrder>> orders,
                                                   List<? extends Element<? extends RemovableOrder>> hiddenOrders,
                                                   UUID removedOrderId,
                                                   String reason) {
        List<Element<RemovableOrder>> updatedHiddenOrders = new ArrayList<>();

        hiddenOrders.forEach(orderElement ->
            updatedHiddenOrders.add(element(orderElement.getId(), orderElement.getValue())));

        orders.stream()
            .filter(order -> removedOrderId.equals(order.getId()))
            .findFirst()
            .ifPresent(order -> {
                orders.remove(order);
                order.getValue().setRemovalReason(reason);
                updatedHiddenOrders.add(element(order.getId(), order.getValue()));
            });

        return updatedHiddenOrders;
    }

    public List<Element<Child>> removeFinalOrderPropertiesFromChildren(CaseData caseData) {
        UUID id = getDynamicListSelectedValue(caseData.getRemovableOrderList(), mapper);
        List<Element<Child>> children = caseData.getAllChildren();

        findElement(id, caseData.getOrderCollection());

        Optional<Element<GeneratedOrder>> removedOrder = findElement(id, caseData.getOrderCollection());

        if (removedOrder.isEmpty()) {
            throw new IllegalStateException("Failed to find the order to be removed");
        }

        if (!removedOrder.get().getValue().isFinalOrder()) {
            return children;
        }

        List<UUID> removedChildrenIDList = removedOrder.get().getValue().getChildrenIDs();

        return children.stream()
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

    public RemovableOrder getRemovedOrderByUUID(CaseData caseData, UUID removedOrderId) {
        return getRemovableOrderList(caseData).stream()
            .filter(orderElement -> removedOrderId.equals(orderElement.getId()))
            .map(Element::getValue)
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Failed to find order matching id " + removedOrderId));
    }

    public List<Element<HearingBooking>> removeHearingLinkedToCMO(List<Element<HearingBooking>> hearings,
                                                                  UUID removedOrderId) {
        return hearings.stream()
            .map(element -> {
                HearingBooking hearingBooking = element.getValue();

                if (removedOrderId.equals(hearingBooking.getCaseManagementOrderId())) {
                    hearingBooking.setCaseManagementOrderId(null);
                }

                return element;
            }).collect(Collectors.toList());
    }

    private List<Element<? extends RemovableOrder>> getRemovableOrderList(CaseData caseData) {
        List<Element<? extends RemovableOrder>> orders =  new ArrayList<>();

        orders.addAll(caseData.getOrderCollection());
        orders.addAll(caseData.getSealedCMOs());

        return orders;
    }
}
