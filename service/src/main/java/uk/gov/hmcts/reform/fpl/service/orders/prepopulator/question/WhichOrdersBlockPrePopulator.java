package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.DischargeCareOrderService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_ORDERS;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WhichOrdersBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private final DischargeCareOrderService dischargeCareOrder;

    @Override
    public OrderQuestionBlock accept() {
        return WHICH_ORDERS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        final List<GeneratedOrder> careOrders = dischargeCareOrder.getManageOrderCareOrders(caseData);
        final Selector childSelector = newSelector(careOrders.size());

        return Map.of(
            "careOrderSelector", childSelector,
            "orders_label", dischargeCareOrder.getOrdersLabel(careOrders)
        );
    }
}
