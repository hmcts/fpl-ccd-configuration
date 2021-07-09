package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.events.order.ManageOrdersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

@Getter
@RequiredArgsConstructor
public class AmendedOrderEvent implements ManageOrdersEvent {
    private final CaseData caseData;
    private final AmendableOrder amendableOrder;
}
