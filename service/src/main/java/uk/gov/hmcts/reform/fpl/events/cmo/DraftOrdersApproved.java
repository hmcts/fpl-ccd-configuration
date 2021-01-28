package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

@Value
public class DraftOrdersApproved {
    CaseData caseData;
    CaseData caseDataBefore;
    HearingBooking hearing;
}
