package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
public class GeneratedOrderRemovalAction implements OrderRemovalAction {

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof GeneratedOrder;
    }

    @Override
    public void remove(CaseData caseData,
                       CaseDetailsMap data, UUID removedOrderId,
                       RemovableOrder removableOrder) {

        GeneratedOrder generatedRemovableOrder = (GeneratedOrder) removableOrder;
        ((GeneratedOrder) removableOrder).toBuilder().judgeAndLegalAdvisor(null).build();

        List<Element<GeneratedOrder>> generatedOrders = caseData.getOrderCollection();
        boolean removed = generatedOrders.remove(element(removedOrderId, generatedRemovableOrder));

        if (!removed) {
            throw new IllegalArgumentException(format("Failed to find order matching id %s", removedOrderId));
        }

        generatedRemovableOrder.setRemovalReason(caseData.getReasonToRemoveOrder());

        List<Element<GeneratedOrder>> hiddenGeneratedOrders = caseData.getHiddenOrders();
        hiddenGeneratedOrders.add(element(removedOrderId, generatedRemovableOrder));

        data.put("children1", removeFinalOrderPropertiesFromChildren(caseData, generatedRemovableOrder));
        data.put("hiddenOrders", hiddenGeneratedOrders);
        data.putIfNotEmpty("orderCollection", generatedOrders);
    }

    @Override
    public void populateCaseFields(CaseData caseData,
                                   CaseDetailsMap data,
                                   UUID removableOrderId,
                                   RemovableOrder removableOrder) {
        GeneratedOrder generatedRemovableOrder = (GeneratedOrder) removableOrder;


        data.put("orderToBeRemoved", generatedRemovableOrder.getDocument());
        data.put("orderTitleToBeRemoved", generatedRemovableOrder.getTitle());
        data.put("orderIssuedDateToBeRemoved", generatedRemovableOrder.getDateOfIssue());
        data.put("orderDateToBeRemoved", generatedRemovableOrder.getDate());
    }

    private List<Element<Child>> removeFinalOrderPropertiesFromChildren(CaseData caseData,
                                                                       GeneratedOrder removedOrder) {
        if (!removedOrder.isFinalOrder()) {
            return caseData.getAllChildren();
        }

        List<UUID> removedChildrenIDList = removedOrder.getChildrenIDs();

        return caseData.getAllChildren().stream()
            .map(element -> {
                if (removedChildrenIDList.contains(element.getId())) {
                    Child child = element.getValue();
                    child.setFinalOrderIssued(null);
                    child.setFinalOrderIssuedType(null);
                }
                return element;
            }).collect(Collectors.toList());
    }
}
