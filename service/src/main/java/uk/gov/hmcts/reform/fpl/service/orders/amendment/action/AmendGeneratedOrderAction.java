package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendGeneratedOrderAction implements AmendOrderAction {
    private static final String CASE_FIELD = "orderCollection";

    private final Time time;

    @Override
    public boolean accept(CaseData caseData) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
        return caseData.getOrderCollection().stream().anyMatch(order -> Objects.equals(selectedOrderId, order.getId()));
    }

    @Override
    public Map<String, Object> applyAmendedOrder(CaseData caseData, DocumentReference amendedOrder) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
        List<Element<GeneratedOrder>> orders = caseData.getOrderCollection();

        int idx = -1;
        GeneratedOrder orderToAmend = null;
        for (int i = 0; i < orders.size(); i++) {
            Element<GeneratedOrder> order = orders.get(i);
            if (Objects.equals(selectedOrderId, order.getId())) {
                idx = i;
                orderToAmend = order.getValue();
                break;
            }
        }

        GeneratedOrder amended = Objects.requireNonNull(orderToAmend).toBuilder()
            .document(amendedOrder)
            .amendedDate(time.now().toLocalDate())
            .build();

        orders.set(idx, element(selectedOrderId, amended));

        return Map.of(CASE_FIELD, orders);
    }
}
