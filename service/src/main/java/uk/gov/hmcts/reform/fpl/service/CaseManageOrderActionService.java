package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;


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

    public void prepareUpdatedDraftCMOForAction(final String authorization, final String userId,
                                                final CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(caseData);

        Document orderDoc = prepareUpdatedDraftCMODocumentForAction(authorization, userId, caseDetails);
        DocumentReference orderDocumentReference = buildCMODocumentReference(orderDoc);

        final String caseManageActionKey = "caseManagementOrderAction";
        if (caseManagementOrder.getCaseManagementOrderAction() != null) {
            caseDetails.getData().put(caseManageActionKey, caseManagementOrder.getCaseManagementOrderAction());
        } else {
            caseDetails.getData().put(caseManageActionKey, ImmutableMap.of("orderDoc", orderDocumentReference));
        }
    }

    public CaseManagementOrderAction getCaseManagementOrderActioned(final String authorization,
                                                                    final String userId,
                                                                    final CaseDetails caseDetails) {
        Map<String, Object> cmoDocumentTemplateData = null;
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseManagementOrder updatedDraftCaseManagementOrder = draftCMOService.prepareCMO(caseDetails.getData());

        CaseManagementOrderAction caseManagementOrderAction =
            updatedDraftCaseManagementOrder.getCaseManagementOrderAction();

        DynamicList list = objectMapper.convertValue(caseDetails.getData().get("cmoHearingDateList"),
            DynamicList.class);

        String hearingDate = null;
        UUID id = null;

        if (list != null) {

            hearingDate = list.getValue().getLabel();
            id = list.getValue().getCode();
        }

        return caseManagementOrderAction.toBuilder()
            .id(id)
            .nextHearingDate(hearingDate)
            .build();
    }

    private DocumentReference buildCMODocumentReference(final Document updatedDocument) {
        return DocumentReference.builder()
            .url(updatedDocument.links.self.href)
            .binaryUrl(updatedDocument.links.binary.href)
            .filename(updatedDocument.originalDocumentName)
            .build();
    }

    private Document prepareUpdatedDraftCMODocumentForAction(final String authorization, final String userId,
                                                             final CaseDetails caseDetails) {
        Map<String, Object> cmoDocumentTemplateData = null;
        try {
            cmoDocumentTemplateData = draftCMOService.generateCMOTemplateData(caseDetails.getData());
        } catch (IOException e) {
            log.error("Unable to generate CMO template data.", e);
        }

        return getDocument(authorization, userId, cmoDocumentTemplateData, false);
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
