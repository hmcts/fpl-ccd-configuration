package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendCaseManagementOrderAction implements AmendOrderAction {
    private static final String CASE_FIELD = "sealedCMOs";

    private final Time time;

    @Override
    public boolean accept(CaseData caseData) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
        return caseData.getSealedCMOs().stream().anyMatch(cmo -> Objects.equals(selectedOrderId, cmo.getId()));
    }

    @Override
    public Map<String, Object> applyAmendedOrder(CaseData caseData, DocumentReference amendedDocument) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
        List<Element<HearingOrder>> orders = caseData.getSealedCMOs();

        int idx = -1;
        HearingOrder orderToAmend = null;
        for (int i = 0; i < orders.size(); i++) {
            Element<HearingOrder> order = orders.get(i);
            if (Objects.equals(selectedOrderId, order.getId())) {
                idx = i;
                orderToAmend = order.getValue();
                break;
            }
        }

        HearingOrder amended = Objects.requireNonNull(orderToAmend).toBuilder()
            .order(amendedDocument)
            .amendedDate(time.now().toLocalDate())
            .build();

        orders.set(idx, element(selectedOrderId, amended));

        return Map.of(CASE_FIELD, orders);
    }
}
