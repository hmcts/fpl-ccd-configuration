package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Value
public class CaseNumberAdded {
    private CaseDetails caseDetails;
}
