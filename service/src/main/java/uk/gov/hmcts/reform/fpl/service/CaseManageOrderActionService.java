package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Map;

@Slf4j
@Service
public class CaseManageOrderActionService {
    private final ObjectMapper objectMapper;
    private final DraftCMOService draftCMOService;

    public CaseManageOrderActionService(ObjectMapper objectMapper, DraftCMOService draftCMOService) {
        this.objectMapper = objectMapper;
        this.draftCMOService = draftCMOService;
    }

    public CaseManagementOrder addDocumentToCaseManagementOrder(final CaseManagementOrder caseManagementOrder,
                                                                final Document documentToAdd) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildCMODocumentReference(documentToAdd))
            .build();
    }

    public CaseManagementOrder getCaseManagementOrderForAction(Map<String, Object> caseDataMap) {
        CaseData caseData = objectMapper.convertValue(caseDataMap, CaseData.class);

        caseDataMap.putAll(draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseData.getCaseManagementOrder(), caseData.getHearingDetails()));

        return objectMapper.convertValue(
            caseDataMap.get("caseManagementOrder"), CaseManagementOrder.class);
    }

    private DocumentReference buildCMODocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }
}
