package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ActionCmoService {
    private final DraftCMOService draftCMOService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final CMODocmosisTemplateDataGenerationService cmoDocmosisTemplateDataGenerationService;

    private static final String CMO_ACTION_KEY = "orderAction";
    private static final String CMO_KEY = "caseManagementOrder";

    //TODO: this should all exist in one CaseManagementOrderService
    public ActionCmoService(DraftCMOService draftCMOService,
                            DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                            CMODocmosisTemplateDataGenerationService cmoDocmosisTemplateDataGenerationService) {
        this.draftCMOService = draftCMOService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.cmoDocmosisTemplateDataGenerationService = cmoDocmosisTemplateDataGenerationService;
    }

    public CaseManagementOrder addDocument(CaseManagementOrder caseManagementOrder, Document document) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildDocumentReference(document))
            .build();
    }

    public void prepareCaseDetailsForSubmission(CaseDetails caseDetails, CaseManagementOrder order, boolean approved) {
        caseDetails.getData().put(CMO_ACTION_KEY, order.getAction());

        if (approved) {
            caseDetails.getData().put(CMO_KEY, order);
        } else {
            caseDetails.getData().remove(CMO_KEY);
        }
    }

    public Map<String, Object> extractMapFieldsFromCaseManagementOrder(CaseManagementOrder order,
                                                                       List<Element<HearingBooking>> hearingDetails) {
        return draftCMOService.extractIndividualCaseManagementOrderObjects(order, hearingDetails);
    }

    public DocmosisDocument getDocmosisDocument(CaseData data, boolean approved) throws IOException {
        Map<String, Object> cmoDocumentTemplateData = cmoDocmosisTemplateDataGenerationService.getTemplateData(data);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            cmoDocumentTemplateData, DocmosisTemplates.CMO);

        String documentTitle = (approved ? document.getDocumentTitle() : "draft-" + document.getDocumentTitle());
        return document.toBuilder()
            .documentTitle(documentTitle)
            .build();
    }

    private DocumentReference buildDocumentReference(final Document document) {
        return DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();
    }
}
