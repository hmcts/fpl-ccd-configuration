package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseDocument;

import java.util.List;

@Component
public class CafcassApiCaseDocumentsConverter implements CafcassApiCaseDataConverter {
    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData, CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.caseDocuments(getCafcassApiCaseDocument(caseData));
    }

    private List<CafcassApiCaseDocument> getCafcassApiCaseDocument(CaseData caseData) {
        //TODO
        return List.of();
    }
}
