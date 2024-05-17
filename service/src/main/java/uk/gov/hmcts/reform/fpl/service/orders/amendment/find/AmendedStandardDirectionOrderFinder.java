package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;

import java.util.Objects;
import java.util.Optional;

@Component
public final class AmendedStandardDirectionOrderFinder implements AmendedOrderFinder<StandardDirectionOrder> {

    @Override
    public Optional<StandardDirectionOrder> findOrderIfPresent(CaseData caseData, CaseData caseDataBefore) {
        StandardDirectionOrder currentOrder = caseData.getStandardDirectionOrder();
        StandardDirectionOrder oldOrder = caseDataBefore.getStandardDirectionOrder();

        return (currentOrder == null || oldOrder == null
                || Objects.equals(currentOrder.getOrderDoc(), oldOrder.getOrderDoc()))
            ? Optional.empty() : Optional.of(currentOrder);
    }
}
