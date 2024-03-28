package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;

@Value
public class DraftOrdersApproved implements ReviewCMOEvent {
    CaseData caseData;
    List<HearingOrder> approvedOrders;
    List<Element<HearingOrder>> approvedConfidentialOrders;
}
