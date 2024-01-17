package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.IsFinalOrder;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.updaters.ChildrenSmartFinalOrderUpdater;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrdersClosedCaseFieldGenerator {
    private final Time time;
    private final ChildrenSmartFinalOrderUpdater childrenSmartFinalOrderUpdater;

    public Map<String, Object> generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        Order order = manageOrdersEventData.getManageOrdersType();

        Map<String, Object> data = new HashMap<>();

        boolean isFinalOrder = IsFinalOrder.YES.equals(order.getIsFinalOrder())
            || BooleanUtils.toBoolean(manageOrdersEventData.getManageOrdersIsFinalOrder());

        if (isFinalOrder) {
            data.put("children1", childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData));
        }

        boolean shouldCloseCase = BooleanUtils.toBoolean(manageOrdersEventData.getManageOrdersCloseCase());
        if (shouldCloseCase && isFinalOrder) {

            data.put("state", CLOSED);
            data.put("closeCaseTabField", CloseCase.builder().date(getCloseCaseDate(manageOrdersEventData))
                .build());
        }

        return data;
    }

    private LocalDate getCloseCaseDate(ManageOrdersEventData manageOrdersEventData) {
        if (manageOrdersEventData.getManageOrdersApprovalDate() != null) {
            return manageOrdersEventData.getManageOrdersApprovalDate();
        } else if (manageOrdersEventData.getManageOrdersApprovalDateOrDateTime() != null) {
            return manageOrdersEventData.getManageOrdersApprovalDateOrDateTime().toLocalDate();
        } else {
            return time.now().toLocalDate();
        }
    }

}
