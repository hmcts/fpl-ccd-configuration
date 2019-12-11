package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

@Service
public class ActionCmoService {

    public static final String SHARED_DRAFT_CMO_DOCUMENT_KEY = "sharedDraftCMODocument";
    private static final String LA_CMO_KEY = "caseManagementOrder";
    private static final String JUDGE_CMO_KEY = "cmoToAction";

    public CaseManagementOrder addDocument(CaseManagementOrder caseManagementOrder, Document document) {
        return caseManagementOrder.toBuilder()
            .orderDoc(buildFromDocument(document))
            .build();
    }

    public CaseManagementOrder addAction(CaseManagementOrder order, OrderAction orderAction) {
        return order.toBuilder()
            .action(orderAction)
            .build();
    }

    // REFACTOR: 10/12/2019 Method name
    public void progressCMOToAction(CaseDetails caseDetails, CaseManagementOrder order) {
        switch (order.getAction().getType()) {
            case SEND_TO_ALL_PARTIES:
                caseDetails.getData().put(SHARED_DRAFT_CMO_DOCUMENT_KEY, order.getOrderDoc());
                break;
            case JUDGE_REQUESTED_CHANGE:
                caseDetails.getData().put(LA_CMO_KEY, order);
                caseDetails.getData().remove(JUDGE_CMO_KEY);
                break;
            case SELF_REVIEW:
                break;
        }
    }

    public Map<String, Object> extractMapFieldsFromCaseManagementOrder(CaseManagementOrder order) {
        if (isNull(order)) {
            order = CaseManagementOrder.builder().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("schedule", order.getSchedule());
        data.put("recitals", order.getRecitals());
        data.put("orderAction", order.getAction());

        return data;
    }
}
