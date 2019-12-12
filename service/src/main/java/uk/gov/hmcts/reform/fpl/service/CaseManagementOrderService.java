package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

//TODO: this class will take some of the methods out of draftCMO service.
@Service
public class CaseManagementOrderService {
    public CaseManagementOrder addDocument(CaseManagementOrder caseManagementOrder, Document document) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildFromDocument(document))
            .build();
    }

    public CaseManagementOrder addAction(CaseManagementOrder order, OrderAction orderAction) {
        return order.toBuilder()
            .action(orderAction)
            .build();
    }

    public Map<String, Object> extractMapFieldsFromCaseManagementOrder(CaseManagementOrder order) {
        if (isNull(order)) {
            order = CaseManagementOrder.builder().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put(SCHEDULE.getKey(), order.getSchedule());
        data.put(RECITALS.getKey(), order.getRecitals());
        data.put(ORDER_ACTION.getKey(), order.getAction());

        return data;
    }

    public OrderAction removeDocumentFromOrderAction(OrderAction orderAction) {
        return orderAction.toBuilder().document(null).build();
    }
}
