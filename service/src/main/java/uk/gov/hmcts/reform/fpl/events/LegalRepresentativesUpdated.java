package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
public class LegalRepresentativesUpdated {
    CaseData caseData;
    CaseData caseDataBefore;
}
