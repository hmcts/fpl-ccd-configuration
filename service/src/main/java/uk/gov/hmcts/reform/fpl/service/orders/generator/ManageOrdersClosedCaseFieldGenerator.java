package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersClosedCaseFieldGenerator {
    private final Time time;
    private final ChildrenService childrenService;

    public Map<String, Object> generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        Order order = manageOrdersEventData.getManageOrdersType();
        boolean closeCase = BooleanUtils.toBoolean(manageOrdersEventData.getManageOrdersCloseCase());

        Map<String, Object> data = new java.util.HashMap<>();

        if (closeCase) {
            data.put("state", "CLOSED");
            data.put("closeCaseTabField", CloseCase.builder().date(time.now().toLocalDate()).build());
        }
        if (order.isFinalOrder()) {
            data.put("children1", getUpdatedChildren(caseData));
        }

        return data;
    }

    private List<Element<Child>> getUpdatedChildren(CaseData caseData) {
        return childrenService.updateFinalOrderIssued(
            caseData.getManageOrdersEventData(),
            caseData.getAllChildren(),
            caseData.getOrderAppliesToAllChildren(),
            caseData.getChildSelector(),
            caseData.getRemainingChildIndex()
        );
    }
}
