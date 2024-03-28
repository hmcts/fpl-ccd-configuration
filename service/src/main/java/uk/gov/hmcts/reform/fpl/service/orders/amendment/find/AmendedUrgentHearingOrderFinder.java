package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.util.Objects;
import java.util.Optional;

@Component
public final class AmendedUrgentHearingOrderFinder implements AmendedOrderFinder<UrgentHearingOrder> {

    @Override
    public Optional<UrgentHearingOrder> findOrderIfPresent(CaseData caseData, CaseData caseDataBefore) {
        UrgentHearingOrder currentOrder = caseData.getUrgentHearingOrder();
        UrgentHearingOrder oldOrder = caseDataBefore.getUrgentHearingOrder();

        return (currentOrder == null || oldOrder == null
                || Objects.equals(currentOrder.getOrder(), oldOrder.getOrder()))
            ? Optional.empty() : Optional.of(currentOrder);
    }
}
