package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;

@Service
public class ManageDocumentService {

    public ManageDocument buildInitialManageDocument(CaseData caseData) {
        return ManageDocument.builder()
            .hearingList(caseData.buildDynamicHearingList())
            .build();
    }
}
