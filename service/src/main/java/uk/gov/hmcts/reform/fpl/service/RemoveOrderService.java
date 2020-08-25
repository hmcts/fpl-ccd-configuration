package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

@Service
public class RemoveOrderService {
    public DynamicList getDropDownListOfExistingOrders(CaseData caseData) {
        List<Element<GeneratedOrder>> orderCollection = caseData.getOrderCollection();

        return ElementUtils.asDynamicList(orderCollection, e -> e.getTitle() + " - " + e.getDateOfIssue());
    }

//    public CaseData updateCaseData(CaseData caseData) {
//
//        List<UUID> selectedOrderIdsForRemoval = new ArrayList<>();
//        caseData.getSelectedOrderIdsForRemoval()
//            .forEach(e -> selectedOrderIdsForRemoval.add(e.getId()));
//
//        List<Element<GeneratedOrder>> ordersCollection = caseData.getOrderCollection();
//        List<Element<GeneratedOrder>> removedOrdersCollection = caseData.getRemovedOrdersCollection();
//
//        ordersCollection
//            .forEach(order -> {
//                if(selectedOrderIdsForRemoval.contains(order.getId())) {
//                    removedOrdersCollection.add(order);
//                    ordersCollection.remove(order);
//                }
//            });
//
//        return caseData.toBuilder()
//            .removedOrdersCollection(removedOrdersCollection)
//            .orderCollection(ordersCollection)
//            .build();
//    }
}
