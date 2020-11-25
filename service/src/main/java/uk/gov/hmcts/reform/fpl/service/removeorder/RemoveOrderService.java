package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.RemovableOrderNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public void populateSelectedOrderFields(CaseData caseData,
                                            CaseDetailsMap data,
                                            UUID removedOrderId,
                                            RemovableOrder removableOrder) {
        orderRemovalActions.getAction(removedOrderId, removableOrder)
            .populateCaseFields(caseData, data, removedOrderId, removableOrder);
    }

    public void removeOrderFromCase(CaseData caseData,
                                    CaseDetailsMap data,
                                    UUID removedOrderId,
                                    RemovableOrder removableOrder) {
        orderRemovalActions.getAction(removedOrderId, removableOrder)
            .remove(caseData, data, removedOrderId, removableOrder);
    }

    public RemovableOrder getRemovedOrderByUUID(CaseData caseData, UUID removedOrderId) {
        return getRemovableOrderList(caseData).stream()
            .filter(orderElement -> removedOrderId.equals(orderElement.getId()))
            .map(Element::getValue)
            .findAny()
            .orElseThrow(() -> new RemovableOrderNotFoundException(removedOrderId));
    }

    private List<Element<? extends RemovableOrder>> getRemovableOrderList(CaseData caseData) {
        List<Element<? extends RemovableOrder>> orders = new ArrayList<>();
        orders.addAll(caseData.getOrderCollection());
        orders.addAll(caseData.getSealedCMOs());
        return orders;
    }
}
