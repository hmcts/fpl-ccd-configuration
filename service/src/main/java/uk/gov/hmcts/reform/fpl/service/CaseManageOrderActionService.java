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

    public CaseManagementOrder prepareActionedCMO(final String authorization, final String userId,
                                                  final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseManagementOrder updatedDraftCaseManagementOrder = caseData.getCaseManagementOrder();
        CaseData updatedCaseData = caseData.toBuilder()
            .caseManagementOrder(updatedDraftCaseManagementOrder)
            .build();

        return getActionedCMOWithDocumentIncluded(
            authorization, userId, caseDetails, updatedCaseData);
    }

    private CaseManagementOrder getActionedCMOWithDocumentIncluded(final String authorization,
                                                                  final String userId,
                                                                  final CaseDetails caseDetails,
                                                                  final CaseData updatedCaseData) {
        Map<String, Object> cmoDocumentTemplateData = null;
        boolean judgeApprovedDraftCMO = false;
        try {
            cmoDocumentTemplateData = draftCMOService.generateCMOTemplateData(caseDetails.getData());
            judgeApprovedDraftCMO = hasJudgeApprovedDraftCMO(updatedCaseData.getCaseManagementOrder());
        } catch (IOException e) {
            log.error("Unable to generate CMO template data.", e);
        }

        Document updatedDocument = getActionedCMODocument(authorization, userId,
            cmoDocumentTemplateData, judgeApprovedDraftCMO);
        return updatedCaseData.getCaseManagementOrder().toBuilder()
            .orderDoc(buildCMODocumentReference(updatedDocument))
            .build();
    }

    private Document getActionedCMODocument(final String authorization, final String userId,
                                           final Map<String, Object> cmoDocumentTemplateData,
                                           final boolean judgeApprovedDraftCMO) {
        final DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            cmoDocumentTemplateData, DocmosisTemplates.CMO);
        final String docTitle = (judgeApprovedDraftCMO
            ? document.getDocumentTitle() : "draft-" + document.getDocumentTitle());

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), docTitle);
    }

    private boolean hasJudgeApprovedDraftCMO(final CaseManagementOrder caseManagementOrder) {
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
