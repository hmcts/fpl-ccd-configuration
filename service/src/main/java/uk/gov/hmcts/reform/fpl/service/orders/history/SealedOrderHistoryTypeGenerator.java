package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C43ChildArrangementOrderDocumentParameterGenerator;

@Component
public class SealedOrderHistoryTypeGenerator {

    public String generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        if (Order.C43_CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER
            .equals(manageOrdersEventData.getManageOrdersType())) {
            return C43ChildArrangementOrderDocumentParameterGenerator.getOrderTitle(manageOrdersEventData) + " (C43)";
        }

        return manageOrdersEventData.getManageOrdersType().getHistoryTitle();
    }
}
