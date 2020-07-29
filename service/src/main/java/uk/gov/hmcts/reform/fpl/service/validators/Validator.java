package uk.gov.hmcts.reform.fpl.service.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

public interface Validator {

    List<String> validate(CaseData caseData);
}
