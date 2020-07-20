package uk.gov.hmcts.reform.fpl.controllers.guards;

import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

public interface EventValidator {

    public List<String> validate(CaseData caseData);
}
