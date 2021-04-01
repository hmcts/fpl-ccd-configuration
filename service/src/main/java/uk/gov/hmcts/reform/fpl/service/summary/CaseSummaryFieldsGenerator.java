package uk.gov.hmcts.reform.fpl.service.summary;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

public interface CaseSummaryFieldsGenerator {

    SyntheticCaseSummary generate(CaseData caseData);
}
