package uk.gov.hmcts.reform.fpl.events.legalcounsel;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;

@Value
public class LegalCounsellorRemoved implements LegalCounsellorEvent {
    CaseData caseData;
    String solicitorOrganisationName;
    LegalCounsellor legalCounsellor;
    CaseData previousCaseData;

    public LegalCounsellorRemoved(CaseData caseData,
                                  String solicitorOrganisationName,
                                  LegalCounsellor legalCounsellor) {
        this(caseData, solicitorOrganisationName, legalCounsellor, caseData);
    }

    public LegalCounsellorRemoved(CaseData caseData,
                                  String solicitorOrganisationName,
                                  LegalCounsellor legalCounsellor,
                                  CaseData previousCaseData) {
        this.caseData = caseData;
        this.solicitorOrganisationName = solicitorOrganisationName;
        this.legalCounsellor = legalCounsellor;
        this.previousCaseData = previousCaseData;
    }
}
