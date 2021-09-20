package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public final class AmendedCaseManagementOrderFinder implements AmendedOrderFinder<HearingOrder> {

    @Override
    public Optional<HearingOrder> findOrderIfPresent(CaseData caseData, CaseData caseDataBefore) {
        List<Element<HearingOrder>> currentOrders = new ArrayList<>(caseData.getSealedCMOs());
        List<Element<HearingOrder>> oldOrders = new ArrayList<>(caseDataBefore.getSealedCMOs());

        currentOrders.removeAll(oldOrders);

        return currentOrders.stream().findFirst().map(Element::getValue);
    }
}
