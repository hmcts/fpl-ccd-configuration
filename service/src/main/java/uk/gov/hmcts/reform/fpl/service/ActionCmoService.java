package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

@Service
public class ActionCmoService {
    private final DraftCMOService draftCMOService;

    private static final String CMO_ACTION_KEY = "orderAction";
    private static final String CMO_KEY = "caseManagementOrder";

    //TODO: this should all exist in one CaseManagementOrderService
    public ActionCmoService(DraftCMOService draftCMOService) {
        this.draftCMOService = draftCMOService;
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

    private DocumentReference buildDocumentReference(final Document document) {
        return DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();
    }
}
