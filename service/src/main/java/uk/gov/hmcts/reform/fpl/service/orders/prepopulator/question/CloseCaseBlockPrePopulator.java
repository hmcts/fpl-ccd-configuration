package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CloseCaseBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final ChildrenService childrenService;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.CLOSE_CASE;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {

        List<Element<Child>> updatedChildren = childrenService.updateFinalOrderIssued(caseData);
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        Order order = manageOrdersEventData.getManageOrdersType();

        if (Order.isOrderFinal(order) && !allChildrenHaveFinalOrder(updatedChildren)) {
            OrderTempQuestions orderTempQuestions = manageOrdersEventData.getOrderTempQuestions();
            return Map.of("orderTempQuestions", orderTempQuestions.toBuilder().closeCase("NO").build());
        }

        return Map.of();
    }

    private boolean allChildrenHaveFinalOrder(List<Element<Child>> updatedChildren) {
        return updatedChildren.stream().allMatch(
            child -> child.getValue().getFinalOrderIssued().equals("Yes")
        );
    }
}
