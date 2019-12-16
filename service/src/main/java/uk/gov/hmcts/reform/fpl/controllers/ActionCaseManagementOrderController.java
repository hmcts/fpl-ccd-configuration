package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.events.CMOEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.CMODocmosisTemplateDataGenerationService;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;
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
    private final CMODocmosisTemplateDataGenerationService templateDataGenerationService;
    private final CoreCaseDataService coreCaseDataService;
    private final DocumentDownloadService documentDownloadService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData()
            .putAll(caseManagementOrderService.extractMapFieldsFromCaseManagementOrder(caseData.getCmoToAction()));

        draftCMOService.prepareCustomDirections(caseDetails, caseData.getCmoToAction());

        caseDetails.getData().put("nextHearingDateList", getHearingDynamicList(caseData.getHearingDetails()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) throws IOException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Document document = getDocument(authorization, userId, caseData, true);

        caseDetails.getData().put("orderAction", OrderAction.builder().document(buildFromDocument(document)).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    //TODO: refactor. far too much logic in this controller now
    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) throws IOException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (sendToAllPartiesBeforeHearingDate(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of(HEARING_NOT_COMPLETED.getValue()))
                .build();
        }

        CaseManagementOrder order = caseData.getCmoToAction();

        order = draftCMOService.prepareCMO(caseData, order).toBuilder()
            .id(order.getId())
            .hearingDate(order.getHearingDate())
            .build();

        OrderAction orderAction = caseManagementOrderService.removeDocumentFromOrderAction(caseData.getOrderAction());

        order = caseManagementOrderService.addAction(order, orderAction);

        order = caseManagementOrderService.addNextHearingToCMO(caseData.getNextHearingDateList(), order);

        caseData = caseData.toBuilder().cmoToAction(order).build();

        Document document = getDocument(authorization, userId, caseData, order.isDraft());

        order = caseManagementOrderService.addDocument(order, document);

        caseDetails.getData().put("nextHearingDateLabel",
            caseManagementOrderService.createNextHearingDateLabel(order, caseData.getHearingDetails()));

        caseDetails.getData().put("cmoToAction", order);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestHeader(value = "authorization") String authorization,
                                     @RequestHeader(value = "user-id") String userId,
                                     @RequestBody CallbackRequest callbackRequest) {
        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change:CMO_PROGRESSION"
        );

        publishEventOnApprovedCMO(authorization, userId, callbackRequest);
    }

    private boolean sendToAllPartiesBeforeHearingDate(CaseData caseData) {
        return caseData.getOrderAction().getType() == SEND_TO_ALL_PARTIES
            && caseManagementOrderService.isHearingDateInFuture(caseData);
    }

    private Document getDocument(String auth, String userId, CaseData data, boolean draft) throws IOException {
        Map<String, Object> cmoDocumentTemplateData = templateDataGenerationService.getTemplateData(data, draft);

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            cmoDocumentTemplateData, DocmosisTemplates.CMO);

        String documentTitle = (draft ? "draft-" + document.getDocumentTitle() : document.getDocumentTitle());

        return uploadDocumentService.uploadPDF(userId, auth, document.getBytes(), documentTitle);
    }

    private DynamicList getHearingDynamicList(List<Element<HearingBooking>> hearingBookings) {
        return draftCMOService.getHearingDateDynamicList(hearingBookings, null);
    }

    private void publishEventOnApprovedCMO(String authorization, String userId, CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        CaseManagementOrder actionedCmo = caseData.getCmoToAction();

        if (!actionedCmo.isDraft()) {
            final String actionCmoDocumentUrl = actionedCmo.getOrderDoc().getBinaryUrl();
            byte[] documentContents = documentDownloadService.downloadDocument(authorization, userId,
                actionCmoDocumentUrl);

            applicationEventPublisher.publishEvent(new CMOEvent(callbackRequest, authorization, userId,
                documentContents));
        }
    }
}
