package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersClosedCaseFieldGenerator {
    private final Time time;
    private final ChildrenService childrenService;

    public Map<String, Object> generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        Order order = manageOrdersEventData.getManageOrdersType();

        Map<String, Object> data = new HashMap<>();

        if (order.isOrderFinal()) {
            data.put("children1", childrenService.updateFinalOrderIssued(caseData));
        }

        boolean shouldCloseCase = BooleanUtils.toBoolean(manageOrdersEventData.getManageOrdersCloseCase());
        if (shouldCloseCase) {
            data.put("state", CLOSED);
            data.put("closeCaseTabField", CloseCase.builder().date(time.now().toLocalDate()).build());
        }

        return data;
    }

}
