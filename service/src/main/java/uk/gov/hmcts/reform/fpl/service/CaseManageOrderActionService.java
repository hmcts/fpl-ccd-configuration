package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.CMOActionType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CaseManageOrderActionService {
    private final ObjectMapper objectMapper;
    private final DraftCMOService draftCMOService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    public CaseManageOrderActionService(ObjectMapper objectMapper,
                                        DraftCMOService draftCMOService,
                                        DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                        UploadDocumentService uploadDocumentService) {
        this.objectMapper = objectMapper;
        this.draftCMOService = draftCMOService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
    }

    public CaseManagementOrder prepareUpdatedDraftCaseManagementOrderForAction(final String authorization,
                                                                               final String userId,
                                                                               final CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(caseData);

        Document orderDoc = getUpdatedDraftCMODocumentForAction(authorization, userId, caseDetails, false);
        DocumentReference orderDocumentReference = buildCMODocumentReference(orderDoc);

        return caseManagementOrder.toBuilder()
            .orderDoc(orderDocumentReference)
            .build();
    }

    public CaseManagementOrderAction getCaseManagementOrderActioned(final String authorization,
                                                                    final String userId,
                                                                    final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseManagementOrder updatedDraftCaseManagementOrder = draftCMOService.prepareCMO(caseDetails.getData());
        CaseData updatedCaseData = caseData.toBuilder()
            .caseManagementOrder(updatedDraftCaseManagementOrder)
            .build();
        boolean judgeApprovedDraftCMO = hasJudgeApprovedDraftCMO(updatedCaseData.getCaseManagementOrder());
        Document updatedDocument = getUpdatedDraftCMODocumentForAction(authorization, userId, caseDetails,
            judgeApprovedDraftCMO);
        CaseManagementOrderAction caseManagementOrderAction =
            updatedDraftCaseManagementOrder.getCaseManagementOrderAction();
        return caseManagementOrderAction.toBuilder()
            .orderDoc(buildCMODocumentReference(updatedDocument))
            .build();
    }

    private DocumentReference buildCMODocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }

    private Document getUpdatedDraftCMODocumentForAction(final String authorization, final String userId,
                                                             final CaseDetails caseDetails,
                                                             boolean draftCMOApprovedByJudge) {
        Map<String, Object> cmoDocumentTemplateData = null;
        try {
            cmoDocumentTemplateData = draftCMOService.generateCMOTemplateData(caseDetails.getData());
        } catch (IOException exception) {
            log.error("Unable to generate CMO template data.", exception);
        }

        return getDocument(authorization, userId, cmoDocumentTemplateData, draftCMOApprovedByJudge);
    }

    private Document getDocument(final String authorization, final String userId,
                                 final Map<String, Object> cmoDocumentTemplateData,
                                 final boolean judgeApprovedDraftCMO) {
        final DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            cmoDocumentTemplateData, DocmosisTemplates.CMO);
        final String documentTitle = (judgeApprovedDraftCMO
            ? document.getDocumentTitle() : "draft-" + document.getDocumentTitle());

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), documentTitle);
    }

    private boolean hasJudgeApprovedDraftCMO(final CaseManagementOrder caseManagementOrder) {
        return CMOActionType.SEND_TO_ALL_PARTIES.equals(
            caseManagementOrder.getCaseManagementOrderAction().getCmoActionType());
    }
}
