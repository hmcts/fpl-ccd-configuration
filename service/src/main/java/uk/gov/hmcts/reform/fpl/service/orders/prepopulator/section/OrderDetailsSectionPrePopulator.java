package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.IsFinalOrder;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ORDER_DETAILS;

@Component
public class OrderDetailsSectionPrePopulator implements OrderSectionPrePopulator {
    @Override
    public OrderSection accept() {
        return ORDER_DETAILS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        Order order = manageOrdersEventData.getManageOrdersType();

        return Map.of(
            "orderDetailsSectionSubHeader", order.getHistoryTitle(),
            "orderTempQuestions", getOrderTempQuestions(manageOrdersEventData, order)
        );
    }

    private OrderTempQuestions getOrderTempQuestions(ManageOrdersEventData manageOrdersEventData, Order order) {
        OrderTempQuestions orderTempQuestions = manageOrdersEventData.getOrderTempQuestions();

        if (IsFinalOrder.MAYBE.equals(order.getIsFinalOrder())) {
            return orderTempQuestions.toBuilder().isFinalOrder("YES").build();
        }

        return orderTempQuestions;
    }
}
