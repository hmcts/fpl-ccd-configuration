package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.fpl.config.GatewayConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.EPO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.FINAL_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Slf4j
@Api
@RestController
@RequestMapping("/callback/create-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderController {

    private final ObjectMapper mapper;
    private final GeneratedOrderService service;
    private final ValidateGroupService validateGroupService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final GatewayConfiguration gatewayConfiguration;
    private final CoreCaseDataService coreCaseDataService;
    private final ChildrenService childrenService;
    private final DocumentDownloadService documentDownloadService;
    private final FeatureToggleService featureToggleService;
    private final Time time;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        final List<String> errors = validateGroupService.validateGroup(caseData,
            ValidateFamilyManCaseNumberGroup.class);

        if (errors.isEmpty()) {
            caseDetails.getData().put("pageShow", caseData.getAllChildren().size() <= 1 ? "No" : "Yes");

            caseDetails.getData().put("dateOfIssue", time.now().toLocalDate());

            if (caseData.getAllocatedJudge() != null) {
                caseDetails.getData().put("judgeAndLegalAdvisor", setAllocatedJudgeLabel(caseData.getAllocatedJudge()));
            }

            if (CLOSED.getValue().equals(caseDetails.getState())) {
                caseDetails.getData()
                    .put("orderTypeAndDocument", OrderTypeAndDocument.builder().type(BLANK_ORDER).build());
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }

    @PostMapping("/populate-selector/mid-event")
    public AboutToStartOrSubmitCallbackResponse handlePopulateSelectorMidEvent(
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (NO.getValue().equals(caseData.getOrderAppliesToAllChildren())) {
            ChildSelector childSelector = ChildSelector.builder().build();
            childSelector.generateChildCount(caseData.getAllChildren().size());

            caseDetails.getData().put("childSelector", childSelector);
            caseDetails.getData().put("children_label", childrenService.getChildrenLabel(caseData.getAllChildren()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    /*
     This mid event is called after:
      • Inputting Judge + LA
      • Adding further directions
      • Close case page
    */
    @PostMapping("/generate-document/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        FurtherDirections orderFurtherDirections = caseData.getOrderFurtherDirections();
        String closeCaseFromOrder = caseData.getCloseCaseFromOrder();
        List<Element<Child>> children;

        if (orderTypeAndDocument.isClosable()) {
            children = getUpdatedChildren(caseData);
        } else {
            children = caseData.getAllChildren();
        }

        // If can display close case, set the flag and return
        if (service.showCloseCase(orderTypeAndDocument, closeCaseFromOrder, children,
            featureToggleService.isCloseCaseEnabled())) {

            data.put("showCloseCaseFromOrderPage", YES);
            data.put("close_case_label", CloseCaseController.LABEL);

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
        }

        if (service.shouldGenerateDocument(orderTypeAndDocument, orderFurtherDirections, children,
            closeCaseFromOrder, featureToggleService.isCloseCaseEnabled())) {
            Document document = getDocument(caseData, DRAFT);

            //Update orderTypeAndDocument with the document so it can be displayed in check-your-answers
            data.put("orderTypeAndDocument", service.buildOrderTypeAndDocument(
                orderTypeAndDocument, document));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge());

        Document document = getDocument(caseData, SEALED);

        List<Element<GeneratedOrder>> orders = caseData.getOrderCollection();

        OrderTypeAndDocument orderTypeAndDocument = service.buildOrderTypeAndDocument(caseData
            .getOrderTypeAndDocument(), document);

        removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

        // Builds an order with custom values based on order type and adds it to list of orders
        orders.add(service.buildCompleteOrder(orderTypeAndDocument, caseData.getOrder(),
            judgeAndLegalAdvisor, caseData.getDateOfIssue(), caseData.getOrderMonths(),
            caseData.getInterimEndDate()));

        data.put("orderCollection", orders);

        if (featureToggleService.isCloseCaseEnabled() && caseData.getOrderTypeAndDocument().getSubtype() != INTERIM) {
            List<Element<Child>> updatedChildren = getUpdatedChildren(caseData);
            data.put("children1", updatedChildren);
        }

        if (caseData.isClosedFromOrder()) {
            data.put("state", CLOSED);
            data.put("closeCaseTabField", CloseCase.builder()
                .date(time.now().toLocalDate())
                .showFullReason(YES)
                .reason(FINAL_ORDER)
                .build());
        }

        service.removeOrderProperties(data);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        DocumentReference mostRecentUploadedDocument = service.getMostRecentUploadedOrderDocument(
            caseData.getOrderCollection());

        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change:SEND_DOCUMENT",
            Map.of("documentToBeSent", mostRecentUploadedDocument)
        );
        applicationEventPublisher.publishEvent(new GeneratedOrderEvent(callbackRequest,
            concatGatewayConfigurationUrlAndMostRecentUploadedOrderDocumentPath(
                mostRecentUploadedDocument.getBinaryUrl()),
            documentDownloadService.downloadDocument(mostRecentUploadedDocument.getBinaryUrl())));
    }

    private JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel(assignedJudgeLabel)
            .build();
    }

    private Document getDocument(CaseData caseData,
                                 OrderStatus orderStatus) {

        DocmosisTemplates templateType = getDocmosisTemplateType(caseData.getOrderTypeAndDocument().getType());

        caseData.setGeneratedOrderStatus(orderStatus);
        DocmosisGeneratedOrder orderTemplateData = service.getOrderTemplateData(caseData);

        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(
            orderTemplateData, templateType);

        OrderTypeAndDocument typeAndDoc = caseData.getOrderTypeAndDocument();

        Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
            service.generateOrderDocumentFileName(typeAndDoc.getType(), typeAndDoc.getSubtype()));

        if (orderStatus == DRAFT) {
            document.originalDocumentName = "draft-" + document.originalDocumentName;
        }

        return document;
    }

    private String concatGatewayConfigurationUrlAndMostRecentUploadedOrderDocumentPath(
        final String mostRecentUploadedOrderDocumentUrl) {
        final String gatewayUrl = gatewayConfiguration.getUrl();

        try {
            URI uri = new URI(mostRecentUploadedOrderDocumentUrl);
            return gatewayUrl + uri.getPath();
        } catch (URISyntaxException e) {
            log.error(mostRecentUploadedOrderDocumentUrl + " url incorrect.", e);
        }
        return "";
    }

    private DocmosisTemplates getDocmosisTemplateType(GeneratedOrderType type) {
        return type == EMERGENCY_PROTECTION_ORDER ? EPO : ORDER;
    }

    private List<Element<Child>> getUpdatedChildren(CaseData caseData) {
        return childrenService.updateFinalOrderIssued(caseData.getAllChildren(),
            caseData.getOrderAppliesToAllChildren(), caseData.getChildSelector());
    }
}
