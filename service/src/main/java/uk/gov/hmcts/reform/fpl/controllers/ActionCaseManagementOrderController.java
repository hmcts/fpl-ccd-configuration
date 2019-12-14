package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.events.CMOEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.service.CMODocmosisTemplateDataGenerationService;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
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

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        final CaseData caseData = mapper.convertValue(data, CaseData.class);

        caseDetails.getData()
            .putAll(caseManagementOrderService.extractMapFieldsFromCaseManagementOrder(caseData.getCmoToAction()));

        draftCMOService.prepareCustomDirections(caseDetails, caseData.getCmoToAction());

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

        Document document = getDocument(authorization, userId, caseData, false);

        caseDetails.getData().put("orderAction", ImmutableMap.of("document", buildFromDocument(document)));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) throws IOException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseManagementOrder order = caseData.getCmoToAction();

        order = draftCMOService.prepareCMO(caseData, order).toBuilder()
            .id(order.getId())
            .hearingDate(order.getHearingDate())
            .build();

        OrderAction orderAction = caseManagementOrderService.removeDocumentFromOrderAction(caseData.getOrderAction());

        order = caseManagementOrderService.addAction(order, orderAction);

        Document document = getDocument(authorization, userId, caseData, false);

        order = caseManagementOrderService.addDocument(order, document);

        caseDetails.getData().put("cmoToAction", order);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestHeader(value = "authorization") String authorization,
                                     @RequestHeader(value = "user-id") String userId,
                                     @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change:CMO_PROGRESSION"
        );

        if (caseData.getCmoToAction().isApprovedByJudge()) {
            final DocmosisDocument document = generateDocmosisDocument(caseData, true);

            applicationEventPublisher.publishEvent(new CMOEvent(callbackRequest, authorization, userId,
                document));
        }
    }

    private Document getDocument(String authorization, String userId, CaseData data, boolean approved)
        throws IOException {
        DocmosisDocument document = generateDocmosisDocument(data, !approved);

        String documentTitle = (approved ? document.getDocumentTitle() : "draft-" + document.getDocumentTitle());

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), documentTitle);
    }

    private DocmosisDocument generateDocmosisDocument(CaseData caseData, boolean draft) throws IOException {
        Map<String, Object> cmoDocumentTemplateData = templateDataGenerationService.getTemplateData(caseData, draft);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(cmoDocumentTemplateData, CMO);
    }
}
