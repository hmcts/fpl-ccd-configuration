package uk.gov.hmcts.reform.fpl.events.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

@Getter
@RequiredArgsConstructor
public class AmendedOrderEvent implements ManageOrdersEvent {
    private final CaseData caseData;
    private final AmendableOrder order;
}
