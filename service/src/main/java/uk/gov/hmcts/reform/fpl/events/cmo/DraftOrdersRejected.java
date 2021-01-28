package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
public class DraftOrdersRejected implements ReviewCMOEvent {
    CaseData caseData;
    CaseData caseDataBefore;
}
