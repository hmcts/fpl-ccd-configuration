package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.GatekeepingOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.StandardDirectionOrderGenerationService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Api
@RestController
@RequestMapping("/callback/add-gatekeeping-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddGatekeepingOrderController extends CallbackController {
    private final DocumentService documentService;
    private final StandardDirectionOrderGenerationService standardDirectionOrderGenerationService;
    private final GatekeepingOrderGenerationService gatekeepingOrderGenerationService;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/generate-draft/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleGenerateDraftMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        DocmosisStandardDirectionOrder templateData = gatekeepingOrderGenerationService.getTemplateData(caseData);
        Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);

        SaveOrSendGatekeepingOrder saveOrSendGatekeepingOrder = caseData.getSaveOrSendGatekeepingOrder().toBuilder()
            .orderDoc(buildFromDocument(document))
            .build();

        caseDetails.getData().put("saveOrSendGatekeepingOrder", saveOrSendGatekeepingOrder);
        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        StandardDirectionOrder order;

        order = StandardDirectionOrder.builder()
            .customDirections(caseData.getSdoDirectionCustom())
            .orderStatus(caseData.getStandardDirectionOrder().getOrderStatus())
            .judgeAndLegalAdvisor(caseData.getJudgeAndLegalAdvisor())
            .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .build();

        //add sdo to case data for document generation
        CaseData updated = caseData.toBuilder().standardDirectionOrder(order).build();

        //generate sdo document
        DocmosisStandardDirectionOrder templateData = gatekeepingOrderGenerationService.getTemplateData(
            updated);
        Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);

        //add document to order
        order.setOrderDocReferenceFromDocument(document);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();
        if (standardDirectionOrder.getOrderStatus() != SEALED) {
            return;
        }

        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change-SEND_DOCUMENT",
            Map.of("documentToBeSent", standardDirectionOrder.getOrderDoc())
        );
        publishEvent(new StandardDirectionsOrderIssuedEvent(caseData));
    }
}
