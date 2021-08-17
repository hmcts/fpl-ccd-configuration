package uk.gov.hmcts.reform.fpl.events.legalcounsel;

import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;

@Value
public class LegalCounsellorRemoved implements LegalCounsellorEvent {
    CaseData caseData;
    String solicitorOrganisationName;
    Pair<String, LegalCounsellor> legalCounsellor;
}
