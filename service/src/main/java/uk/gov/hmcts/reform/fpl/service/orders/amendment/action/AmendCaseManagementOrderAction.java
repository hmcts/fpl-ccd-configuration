package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
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
    public Map<String, Object> applyAmendedOrder(CaseData caseData, DocumentReference amendedDocument, List<Element<Other>> selectedOthers) {
        UUID selectedOrderId = caseData.getManageOrdersEventData().getManageOrdersAmendmentList().getValueCodeAsUUID();
        List<Element<HearingOrder>> orders = caseData.getSealedCMOs();

        orders.stream()
            .filter(order -> Objects.equals(order.getId(), selectedOrderId))
            .findFirst()
            .ifPresent(order -> {
                HearingOrder amended = order.getValue().toBuilder()
                    .order(amendedDocument)
                    .amendedDate(time.now().toLocalDate())
                    .others(selectedOthers)
                    .build();

                orders.set(orders.indexOf(order), element(order.getId(), amended));
            });

        return Map.of(CASE_FIELD, orders);
    }
}
