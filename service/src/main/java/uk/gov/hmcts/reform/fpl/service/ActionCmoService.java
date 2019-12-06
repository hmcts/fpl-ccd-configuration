package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Map;

@Service
public class ActionCmoService {
    private final ObjectMapper objectMapper;
    private final DraftCMOService draftCMOService;

    private static final String CMO_ACTION_KEY = "orderAction";
    private static final String CMO_KEY = "caseManagementOrder";

    //TODO: this should all exist in one CaseManagementOrderService
    public ActionCmoService(ObjectMapper objectMapper, DraftCMOService draftCMOService) {
        this.objectMapper = objectMapper;
        this.draftCMOService = draftCMOService;
    }

    public CaseManagementOrder addDocument(CaseManagementOrder caseManagementOrder, Document document) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildDocumentReference(document))
            .build();
    }

    public CaseManagementOrder getCaseManagementOrder(Map<String, Object> caseDataMap) {
        CaseData caseData = objectMapper.convertValue(caseDataMap, CaseData.class);

        caseDataMap.putAll(draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseData.getCaseManagementOrder(), caseData.getHearingDetails()));

        return objectMapper.convertValue(caseDataMap.get("caseManagementOrder"), CaseManagementOrder.class);
    }

    public void prepareCaseDetailsForSubmission(CaseDetails caseDetails, CaseManagementOrder order, boolean approved) {
        caseDetails.getData().put(CMO_ACTION_KEY, order.getAction());

        if (approved) {
            caseDetails.getData().put(CMO_KEY, order);
        } else {
            caseDetails.getData().remove(CMO_KEY);
        }
    }

    private DocumentReference buildDocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }
}
