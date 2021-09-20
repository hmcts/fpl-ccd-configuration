package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.IsFinalOrder;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.updaters.ChildrenSmartFinalOrderUpdater;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CloseCaseBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final ChildrenSmartFinalOrderUpdater childrenSmartFinalOrderUpdater;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.CLOSE_CASE;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {

        final List<Element<Child>> updatedChildren = childrenSmartFinalOrderUpdater.updateFinalOrderIssued(caseData);
        final ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        final Order order = manageOrdersEventData.getManageOrdersType();

        final OrderTempQuestions orderTempQuestions = ofNullable(manageOrdersEventData.getOrderTempQuestions())
            .orElse(OrderTempQuestions.builder().build());

        boolean canCloseCase = isFinalOrder(manageOrdersEventData, order) && allChildrenHaveFinalOrder(updatedChildren);

        return Map.of("orderTempQuestions", orderTempQuestions.toBuilder()
            .closeCase(canCloseCase ? "YES" : "NO")
            .build());
    }

    private boolean isFinalOrder(ManageOrdersEventData manageOrdersEventData, Order order) {
        return IsFinalOrder.YES.equals(order.getIsFinalOrder())
            || BooleanUtils.toBoolean(manageOrdersEventData.getManageOrdersIsFinalOrder());
    }

    private boolean allChildrenHaveFinalOrder(List<Element<Child>> updatedChildren) {
        return updatedChildren.stream().allMatch(
            child -> child.getValue().getFinalOrderIssued().equals("Yes")
        );
    }
}
