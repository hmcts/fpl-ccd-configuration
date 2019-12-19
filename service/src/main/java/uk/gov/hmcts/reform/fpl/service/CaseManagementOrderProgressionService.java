package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.UUID.randomUUID;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.JUDGE_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_SHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SERVED_CASE_MANAGEMENT_ORDERS;

@Service
public class CaseManagementOrderProgressionService {
    //TODO: better CCD ids for the below:
    // sharedDraftCMODocument -> sharedCaseManagementOrderDocument
    // caseManagementOrder -> draftCaseManagementOrder_LOCAL_AUTHORITY
    // cmoToAction -> draftCaseManagementOrder_JUDICIARY
    // requires changes in CCD definition. Decided not in scope of 24.

    private final ObjectMapper mapper;

    @Autowired
    public CaseManagementOrderProgressionService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void handleCaseManagementOrderProgression(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (localAuthorityIsDrafting(caseData)) {
            progressDraftCaseManagementOrder(caseDetails, caseData.getCaseManagementOrder());
        } else {
            progressActionCaseManagementOrder(caseDetails, caseData);
        }
    }

    private boolean localAuthorityIsDrafting(CaseData caseData) {
        return caseData.getCaseManagementOrder() != null
            && caseData.getCaseManagementOrder().getStatus() != JUDGE_REVIEW;
    }

    private void progressDraftCaseManagementOrder(CaseDetails caseDetails, CaseManagementOrder order) {
        switch (order.getStatus()) {
            case SEND_TO_JUDGE:
                CaseManagementOrder updatedOrder = order.toBuilder()
                    .status(JUDGE_REVIEW)
                    .build();

                caseDetails.getData().put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), updatedOrder);
                caseDetails.getData().remove(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey());
                break;
            case PARTIES_REVIEW:
                caseDetails.getData().put(CASE_MANAGEMENT_ORDER_SHARED.getKey(), order.getOrderDoc());
                break;
            case SELF_REVIEW:
                caseDetails.getData().remove(CASE_MANAGEMENT_ORDER_SHARED.getKey());
                break;
        }
    }

    private void progressActionCaseManagementOrder(CaseDetails caseDetails, CaseData caseData) {
        switch (caseData.getCaseManagementOrder().getAction().getType()) {
            case SEND_TO_ALL_PARTIES:
                List<Element<CaseManagementOrder>> orders = addOrderToList(caseData);

                caseDetails.getData().put(SERVED_CASE_MANAGEMENT_ORDERS.getKey(), orders);
                caseDetails.getData().remove(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey());
                break;
            case JUDGE_REQUESTED_CHANGE:
                CaseManagementOrder updatedOrder = caseData.getCaseManagementOrder().toBuilder()
                    .status(SELF_REVIEW)
                    .build();

                caseDetails.getData().put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), updatedOrder);
                caseDetails.getData().remove(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey());
                break;
            case SELF_REVIEW:
                break;
        }
    }

    private List<Element<CaseManagementOrder>> addOrderToList(CaseData caseData) {
        List<Element<CaseManagementOrder>> orders = caseData.getServedCaseManagementOrders();
        orders.add(0, Element.<CaseManagementOrder>builder()
            .id(randomUUID())
            .value(caseData.getCaseManagementOrder())
            .build());

        return orders;
    }
}
