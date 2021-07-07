package uk.gov.hmcts.reform.fpl.service.orders.history;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C43ChildArrangementOrderTitleGenerator;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SealedOrderHistoryTypeGenerator {

    private final C43ChildArrangementOrderTitleGenerator c43TitleGenerator;

    public String generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        if (Order.C43_CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER
            .equals(manageOrdersEventData.getManageOrdersType())) {
            return c43TitleGenerator.getOrderTitle(manageOrdersEventData) + " (C43)";
        }

        return manageOrdersEventData.getManageOrdersType().getHistoryTitle();
    }
}
