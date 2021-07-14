package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

import java.util.Optional;

public interface AmendedOrderFinder<O extends AmendableOrder> {
    Optional<O> findOrderIfPresent(CaseData caseData, CaseData caseDataBefore);
}
