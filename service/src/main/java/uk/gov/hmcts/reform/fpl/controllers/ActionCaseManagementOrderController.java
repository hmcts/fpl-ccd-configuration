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
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.DATE_OF_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.NEXT_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
import static uk.gov.hmcts.reform.fpl.enums.Event.ACTION_CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

@Api
@RestController
@RequestMapping("/callback/action-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActionCaseManagementOrderController {
    private final DraftCMOService draftCMOService;
    private final CaseManagementOrderService caseManagementOrderService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CaseManagementOrderGenerationService templateDataGenerationService;
    private final CoreCaseDataService coreCaseDataService;
    private final DocumentDownloadService documentDownloadService;
    private final RequestData requestData;
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

        caseDetails.getData().putAll(caseManagementOrderService
            .extractMapFieldsFromCaseManagementOrder(caseManagementOrder));

        draftCMOService.prepareCustomDirections(caseDetails, caseManagementOrder);

        caseDetails.getData().put(NEXT_HEARING_DATE_LIST.getKey(), getHearingDynamicList(caseData.getHearingDetails()));

        caseDetails.getData().put(DATE_OF_ISSUE.getKey(),
            caseManagementOrderService.getIssuedDate(caseManagementOrder));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Document document = getDocument(caseData);

        caseDetails.getData()
            .put(ORDER_ACTION.getKey(), OrderAction.builder().document(buildFromDocument(document)).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseManagementOrder order = caseData.getCaseManagementOrder();
        OrderAction orderAction = caseData.getOrderAction();
        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        if (!order.isInJudgeReview()) {
            caseDetails.getData().remove(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .build();
        }

        if (issuingOrderBeforeHearingDateHasPassed(orderAction, order.getId(), hearingDetails)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(HEARING_NOT_COMPLETED.getValue()))
                .build();
        }

        CaseManagementOrder preparedOrder = draftCMOService.prepareCaseManagementOrder(caseData);
        Document document = getDocument(caseData.toBuilder().caseManagementOrder(preparedOrder).build());

        preparedOrder.setOrderDocReferenceFromDocument(document);

        caseDetails.getData().remove(DATE_OF_ISSUE.getKey());
        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_JUDICIARY.getKey(), preparedOrder);
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
                "internal-change:CMO_PROGRESSION"
            );

            coreCaseDataService.triggerEvent(
                callbackRequest.getCaseDetails().getJurisdiction(),
                callbackRequest.getCaseDetails().getCaseTypeId(),
                callbackRequest.getCaseDetails().getId(),
                "internal-change:SEND_DOCUMENT",
                Map.of("documentToBeSent", caseManagementOrder.getOrderDoc())
            );
            publishEventOnApprovedCMO(callbackRequest);
        }
    }

    private boolean issuingOrderBeforeHearingDateHasPassed(OrderAction action,
                                                           UUID id,
                                                           List<Element<HearingBooking>> hearings) {
        LocalDateTime hearingStartDate = getHearingStartDateForOrderWithId(hearings, id);

        return action.hasActionTypeSendToAllParties() && hearingStartDate.isAfter(LocalDateTime.now());
    }

    private LocalDateTime getHearingStartDateForOrderWithId(List<Element<HearingBooking>> hearings, UUID id) {
        return hearingBookingService.getHearingBookingByUUID(hearings, id).getStartDate();
    }

    private Document getDocument(CaseData data) {
        DocmosisCaseManagementOrder templateData = templateDataGenerationService.getTemplateData(data);
        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, CMO);

        return uploadDocumentService.uploadPDF(document.getBytes(), getDocumentTitle(data, document));
    }

    private String getDocumentTitle(CaseData data, DocmosisDocument document) {
        String documentTitle;

        if (data.getCaseManagementOrder().isDraft()) {
            documentTitle = document.addDraftToTitle();
        } else {
            documentTitle = document.getDocumentTitle();
        }
        return documentTitle;
    }

    private DynamicList getHearingDynamicList(List<Element<HearingBooking>> hearingBookings) {
        return draftCMOService.getHearingDateDynamicList(hearingBookings, null);
    }

    private void publishEventOnApprovedCMO(CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        CaseManagementOrder actionedCmo = caseData.getCaseManagementOrder();

        if (!actionedCmo.isDraft()) {
            final String actionCmoDocumentUrl = actionedCmo.getOrderDoc().getBinaryUrl();
            byte[] documentContents = documentDownloadService.downloadDocument(actionCmoDocumentUrl);

            applicationEventPublisher.publishEvent(new CaseManagementOrderIssuedEvent(callbackRequest, requestData,
                documentContents));
        }
    }
}
