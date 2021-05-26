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
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.GatekeepingOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderEventNotificationDecider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Api
@RestController
@RequestMapping("/callback/add-gatekeeping-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddGatekeepingOrderController extends CallbackController {
    private final DocumentService documentService;
    private final GatekeepingOrderGenerationService gatekeepingOrderGenerationService;
    private final CoreCaseDataService coreCaseDataService;
    private final GatekeepingOrderEventNotificationDecider notificationDecider;

    @PostMapping("/generate-draft/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleGenerateDraftMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        DocmosisStandardDirectionOrder templateData = gatekeepingOrderGenerationService.getTemplateData(caseData);
        Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);

        //generate draft document
        SaveOrSendGatekeepingOrder saveOrSendGatekeepingOrder = caseData.getSaveOrSendGatekeepingOrder().toBuilder()
            .draftDocument(buildFromDocument(document))
            .orderStatus(null)
            .build();

        //check if missing fields for seal and send
        if (caseData.getFirstHearing().isEmpty() || isEmpty(caseData.getAllocatedJudge())) {
            saveOrSendGatekeepingOrder = saveOrSendGatekeepingOrder.toBuilder()
                .nextSteps(buildNextStepsLabel(caseData))
                .build();
        }

        caseDetails.getData().put("saveOrSendGatekeepingOrder", saveOrSendGatekeepingOrder);
        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        //generate document
        DocmosisStandardDirectionOrder templateData = gatekeepingOrderGenerationService.getTemplateData(
            caseData);
        Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);

        //generate sdo for tab
        caseDetails.getData().put("standardDirectionOrder", StandardDirectionOrder.builder()
            .customDirections(caseData.getSdoDirectionCustom())
            .orderStatus(caseData.getStandardDirectionOrder().getOrderStatus())
            .judgeAndLegalAdvisor(caseData.getJudgeAndLegalAdvisor()) //use transient SDO-only judge field instead?
            .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .orderDoc(buildFromDocument(document))
            .build());

        //remove temp fields here
        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        CaseData caseData = getCaseData(request);
        CaseData caseDataBefore = getCaseDataBefore(request);

        Optional<GatekeepingOrderEvent> event = notificationDecider.buildEventToPublish(
            caseData, caseDataBefore.getState()
        );

        event.ifPresent(eventToPublish -> {
            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                caseData.getId(),
                "internal-change-SEND_DOCUMENT",
                Map.of("documentToBeSent", eventToPublish.getOrder())
            );

            publishEvent(eventToPublish);
        });
    }

    private String buildNextStepsLabel(CaseData caseData) {
        List<String> requiredMissingInformation = new ArrayList<>();
        requiredMissingInformation.add("## Next steps");
        requiredMissingInformation.add("Your order will be saved as a draft in 'Draft orders'.");
        requiredMissingInformation.add("You cannot seal and send the order until adding:");

        if (caseData.getFirstHearing().isEmpty()) {
            requiredMissingInformation.add("* hearing details (1st or 2nd?)");
        }

        if (isEmpty(caseData.getAllocatedJudge())) {
            requiredMissingInformation.add("* the allocated judge");
            if (isEmpty(caseData.getAllocationDecision())) {
                requiredMissingInformation.add("* the allocation decision");
            }
        }

        return String.join("\n\n", requiredMissingInformation);
    }
}
