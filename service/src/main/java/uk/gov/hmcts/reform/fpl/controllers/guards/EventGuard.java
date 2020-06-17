package uk.gov.hmcts.reform.fpl.controllers.guards;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

public interface EventGuard {
    public List<String> validate(CaseDetails caseDetails);
}
