package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

@Getter
@RequiredArgsConstructor
public class CaseManagementOrderIssuedEvent {
    private final CaseData caseData;
    private final HearingOrder cmo;
}
