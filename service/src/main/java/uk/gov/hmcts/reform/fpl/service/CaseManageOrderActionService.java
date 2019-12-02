package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.CMOActionType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Map;

@Service
public class CaseManageOrderActionService {
    private final DraftCMOService draftCMOService;

    public CaseManageOrderActionService(DraftCMOService draftCMOService) {
        this.draftCMOService = draftCMOService;
    }

    public CaseManagementOrder addDocumentToActionedCaseManagementOrder(final String authorization, final String userId,
                                                                final CaseDetails caseDetails,
                                                                final CaseData updatedCaseData) {
        Map<String, Object> cmoDocumentTemplateData = draftCMOService.generateCMOTemplateData(caseDetails.getData());
        boolean cmoApprovedByJudge = draftCMOApprovedByJudge(updatedCaseData.getCaseManagementOrder());
        Document updatedDocument = draftCMOService.getCMODocument(authorization, userId, cmoDocumentTemplateData,
            cmoApprovedByJudge);

        return updatedCaseData.getCaseManagementOrder().toBuilder()
            .orderDoc(buildCMODocumentReference(updatedDocument))
            .build();
    }

    private boolean draftCMOApprovedByJudge(final CaseManagementOrder caseManagementOrder) {
        return CMOActionType.SEND_TO_ALL_PARTIES.equals(
            caseManagementOrder.getCaseManagementOrderAction().getCmoActionType());
    }

    private DocumentReference buildCMODocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }
}
