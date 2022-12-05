package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

@Getter
@RequiredArgsConstructor
public class DraftOrdersRemovedEvent {
    private final CaseData caseData;
    private final CaseData caseDataBefore;
    private final Element<HearingOrder> draftOrderRemoved;
    private final String removalReason;
}
