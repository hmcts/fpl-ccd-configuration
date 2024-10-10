package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;

public interface CafcassApiCaseDataConverter {
    CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                         CafcassApiCaseData.CafcassApiCaseDataBuilder builder);
}
