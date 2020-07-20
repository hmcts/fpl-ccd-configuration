package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.DATE_OF_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.Event.ACTION_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

/**
 * Manages the endpoints for the old action-cmo event. To be removed once we have fully migrated away from the old CMO
 * stuff.
 *
 * @deprecated to be replaced
 */
@Api
@RestController
@RequestMapping("/callback/action-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Deprecated(since = "FPLA-1915")
@SuppressWarnings("java:S1133") // Remove once deprecations dealt with
public class ActionCaseManagementOrderController {
    private final CaseManagementOrderService caseManagementOrderService;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CoreCaseDataService coreCaseDataService;
    private final DocumentDownloadService documentDownloadService;
    private final HearingBookingService hearingBookingService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();
        if (caseManagementOrder == null || !caseManagementOrder.isInJudgeReview()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .build();
        }

        caseManagementOrderService.prepareCustomDirections(caseDetails, caseManagementOrder);

        caseDetails.getData().putAll(caseManagementOrder.getCCDFields());
        caseDetails.getData()
            .put(NEXT_HEARING_DATE_LIST.getKey(), caseManagementOrderService.getNextHearingDateDynamicList(caseData));
        caseDetails.getData().put(DATE_OF_ISSUE.getKey(), caseManagementOrder.getDateOfIssueAsDate());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);
        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();

        Document document = caseManagementOrderService.getOrderDocument(caseData);
        caseManagementOrder.setOrderDocReferenceFromDocument(document);

        data.put(ORDER_ACTION.getKey(), OrderAction.builder().document(buildFromDocument(document)).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseManagementOrder order = caseData.getCaseManagementOrder();

        if (order == null || !order.isInJudgeReview()) {
            caseDetails.getData().remove(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .build();
        }

        OrderAction orderAction = caseData.getOrderAction();
        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        if (issuingOrderBeforeHearingDate(orderAction, order.getId(), hearingDetails)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(singletonList(HEARING_NOT_COMPLETED.getValue()))
                .build();
        }

        Document document = caseManagementOrderService.getOrderDocument(caseData);
        order.setOrderDocReferenceFromDocument(document);

        caseDetails.getData().remove(DATE_OF_ISSUE.getKey());
        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), order);
        caseDetails.getData().put("cmoEventId", ACTION_CASE_MANAGEMENT_ORDER.getId());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();
        if (caseManagementOrder != null && caseManagementOrder.isInJudgeReview()) {
            coreCaseDataService.triggerEvent(
                callbackRequest.getCaseDetails().getJurisdiction(),
                callbackRequest.getCaseDetails().getCaseTypeId(),
                callbackRequest.getCaseDetails().getId(),
                "internal-change-CMO_PROGRESSION"
            );

            coreCaseDataService.triggerEvent(
                callbackRequest.getCaseDetails().getJurisdiction(),
                callbackRequest.getCaseDetails().getCaseTypeId(),
                callbackRequest.getCaseDetails().getId(),
                "internal-change-SEND_DOCUMENT",
                Map.of("documentToBeSent", caseManagementOrder.getOrderDoc())
            );
            publishEventOnApprovedCMO(callbackRequest);
        }
    }

    private boolean issuingOrderBeforeHearingDate(OrderAction action, UUID id, List<Element<HearingBooking>> hearings) {
        HearingBooking hearingBooking = hearingBookingService.getHearingBookingByUUID(hearings, id);

        return action.isSendToAllPartiesType() && hearingBooking.startsAfterToday();
    }

    private void publishEventOnApprovedCMO(CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        CaseManagementOrder actionedCmo = caseData.getCaseManagementOrder();

        if (!actionedCmo.isDraft()) {
            final String actionCmoDocumentUrl = actionedCmo.getOrderDoc().getBinaryUrl();
            byte[] documentContents = documentDownloadService.downloadDocument(actionCmoDocumentUrl);

            applicationEventPublisher.publishEvent(
                new CaseManagementOrderIssuedEvent(callbackRequest, documentContents));
        }
    }
}
