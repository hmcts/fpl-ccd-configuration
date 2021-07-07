package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.RemovableOrderOrApplicationNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
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

        List<Element<GeneratedOrder>> generatedOrders = caseData.getOrderCollection();
        boolean removed = generatedOrders.remove(element(removedOrderId, generatedRemovableOrder));

        if (!removed) {
            throw new RemovableOrderOrApplicationNotFoundException(removedOrderId);
        }

        generatedRemovableOrder = generatedRemovableOrder.toBuilder()
            .judgeAndLegalAdvisor(null)
            .removalReason(caseData.getReasonToRemoveOrder())
            .build();

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
        data.put("orderTitleToBeRemoved", defaultIfNull(generatedRemovableOrder.getTitle(),
            generatedRemovableOrder.getType()));
        data.put("orderIssuedDateToBeRemoved", Optional.ofNullable(generatedRemovableOrder.getDateOfIssue())
            .orElseGet(
                () -> formatLocalDateTimeBaseUsingFormat(generatedRemovableOrder.getDateTimeIssued(), "d MMMM yyyy")
            )
        );
        data.put("orderDateToBeRemoved", generatedRemovableOrder.getDate());
        data.put("showRemoveCMOFieldsFlag", NO.getValue());
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
