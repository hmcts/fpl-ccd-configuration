package uk.gov.hmcts.reform.fpl.events.legalcounsel;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;

@Value
public class LegalCounsellorAdded implements LegalCounsellorEvent {
    CaseData caseData;
    LegalCounsellor legalCounsellor;
}
