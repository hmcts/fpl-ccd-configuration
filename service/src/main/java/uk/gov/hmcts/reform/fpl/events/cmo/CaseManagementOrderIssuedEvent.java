package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

@Getter
@RequiredArgsConstructor
public class CaseManagementOrderIssuedEvent implements ReviewCMOEvent {
    private final CaseData caseData;
    private final HearingOrder cmo;
}
