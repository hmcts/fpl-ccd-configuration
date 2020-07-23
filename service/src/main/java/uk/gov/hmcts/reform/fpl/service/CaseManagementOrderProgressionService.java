package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForPartyReviewEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.UUID.randomUUID;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_SHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SERVED_CASE_MANAGEMENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.DRAFT_CASE_MANAGEMENT_ORDER;

/**
 * Service manging the flow of CMO objects between judge and LA.
 *
 * @deprecated to be removed with {@link CaseManagementOrder}
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Deprecated(since = "FPLA-1915")
@SuppressWarnings("java:S1133") // Remove once deprecations dealt with
public class CaseManagementOrderProgressionService {

    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DocumentDownloadService documentDownloadService;

    public void handleCaseManagementOrderProgression(CaseDetails caseDetails, String eventId) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (DRAFT_CASE_MANAGEMENT_ORDER.getId().equals(eventId)) {
            progressDraftCaseManagementOrder(caseDetails, caseData.getCaseManagementOrder());
        } else {
            progressActionCaseManagementOrder(caseDetails, caseData);
        }
    }

    private void progressDraftCaseManagementOrder(CaseDetails caseDetails, CaseManagementOrder order) {
        switch (order.getStatus()) {
            case SEND_TO_JUDGE:
                caseDetails.getData().put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);
                caseDetails.getData().remove(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey());
                publishReadyForJudgeReviewEvent(caseDetails);
                caseDetails.getData().remove(NEXT_HEARING_DATE_LIST.getKey());
                break;
            case PARTIES_REVIEW:
                caseDetails.getData().put(CASE_MANAGEMENT_ORDER_SHARED.getKey(), order.getOrderDoc());
                publishReadyForPartyReviewEvent(caseDetails);
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

                uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder cmo =
                    uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder.builder()
                        .order(updatedOrder.getOrderDoc())
                        .hearing(updatedOrder.getHearingDate())
                        .requestedChanges(updatedOrder.getAction().getChangeRequestedByJudge()).build();
                sendChangesRequestedNotificationToLocalAuthority(caseDetails, cmo);
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

    private void publishReadyForJudgeReviewEvent(CaseDetails caseDetails) {
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        applicationEventPublisher.publishEvent(new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest));
    }

    private void publishReadyForPartyReviewEvent(CaseDetails caseDetails) {
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        applicationEventPublisher.publishEvent(new CaseManagementOrderReadyForPartyReviewEvent(callbackRequest,
            documentDownloadService.downloadDocument(caseData.getSharedDraftCMODocument().getBinaryUrl())));
    }

    @Deprecated
    private void sendChangesRequestedNotificationToLocalAuthority(
        CaseDetails caseDetails,
        uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder cmo) {
        applicationEventPublisher.publishEvent(
            new CaseManagementOrderRejectedEvent(CallbackRequest.builder().caseDetails(caseDetails).build(), cmo));
    }
}
