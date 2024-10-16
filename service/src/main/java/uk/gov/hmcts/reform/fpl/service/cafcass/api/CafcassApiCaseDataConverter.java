package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;

import java.util.List;

public interface CafcassApiCaseDataConverter {

    /**
     * When querying via Elastic Search, retrieving the whole case data should be avoided.
     * If only a sub-set of case information is required, e.g. state, jurisdiction, or only a subset of the case fields,
     * only those should be requested by providing "_source". e.g. "_source": ["jurisdiction", "data.respondents1"]
     * @return A list of string containing the "source" required by this converter.
     * Null or empty list if no case information is required.
     */
    List<String> getEsSearchSources();
    CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                         CafcassApiCaseData.CafcassApiCaseDataBuilder builder);
}
