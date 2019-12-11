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

import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

@Service
public class ActionCmoService {
    private final DraftCMOService draftCMOService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final CMODocmosisTemplateDataGenerationService cmoDocmosisTemplateDataGenerationService;

    private static final String CMO_ACTION_KEY = "orderAction";
    private static final String LA_CMO_KEY = "caseManagementOrder";
    private static final String JUDGE_CMO_KEY = "cmoToAction";

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
            .orderDoc(buildFromDocument(document))
            .build();
    }

    // REFACTOR: 10/12/2019 Method name
    public void progressCMOToAction(CaseDetails caseDetails, CaseManagementOrder order, boolean approved) {
        switch (order.getAction().getType()) {
            case SEND_TO_ALL_PARTIES:
                caseDetails.getData().put("sharedDraftCMODocument", order.getOrderDoc());
                break;
            case JUDGE_REQUESTED_CHANGE:
                caseDetails.getData().put(LA_CMO_KEY, order);
                caseDetails.getData().remove(JUDGE_CMO_KEY);
                break;
            case SELF_REVIEW:
                break;
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
}
