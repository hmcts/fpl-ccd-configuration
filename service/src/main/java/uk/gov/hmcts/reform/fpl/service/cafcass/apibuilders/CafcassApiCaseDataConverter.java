package uk.gov.hmcts.reform.fpl.service.cafcass.apibuilders;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCaseData;

public interface CafcassApiCaseDataConverter {
    CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                         CafcassApiCaseData.CafcassApiCaseDataBuilder builder);
}
