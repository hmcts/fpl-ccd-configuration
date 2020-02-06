package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.fpl.config.GatewayConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.EPO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;

@Slf4j
@Api
@RequestMapping("/callback/create-order")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratedOrderController {
    private final ObjectMapper mapper;
    private final GeneratedOrderService service;
    private final ValidateGroupService validateGroupService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final GatewayConfiguration gatewayConfiguration;
    private final ChildrenService childrenService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        final List<String> errors = validateGroupService.validateGroup(caseData,
            ValidateFamilyManCaseNumberGroup.class);

        if (errors.isEmpty()) {
            childrenService.addPageShowToCaseDetails(caseDetails, caseData.getAllChildren());
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

    @PostMapping("/generate-document/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        FurtherDirections orderFurtherDirections = caseData.getOrderFurtherDirections();

        // Only generate a document if a blank order or further directions has been added
        if (orderTypeAndDocument.getType() == BLANK_ORDER || orderFurtherDirections != null) {
            Document document = getDocument(authorization, userId, caseData);

            //Update orderTypeAndDocument with the document so it can be displayed in check-your-answers
            caseDetails.getData().put("orderTypeAndDocument", service.buildOrderTypeAndDocument(
                orderTypeAndDocument, document));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<GeneratedOrder>> orders = caseData.getOrderCollection();

        // Builds an order with custom values based on order type and adds it to list of orders
        orders.add(service.buildCompleteOrder(caseData.getOrderTypeAndDocument(), caseData.getOrder(),
            caseData.getJudgeAndLegalAdvisor(), caseData.getOrderMonths(), caseData.getInterimEndDate()));

        caseDetails.getData().put("orderCollection", orders);

        service.removeOrderProperties(caseDetails.getData());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestHeader(value = "authorization") String authorization,
                                     @RequestHeader(value = "user-id") String userId,
                                     @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        String mostRecentUploadedDocumentUrl = service.getMostRecentUploadedOrderDocumentUrl(
            caseData.getOrderCollection());

        applicationEventPublisher.publishEvent(new GeneratedOrderEvent(callbackRequest, authorization, userId,
            concatGatewayConfigurationUrlAndMostRecentUploadedOrderDocumentPath(mostRecentUploadedDocumentUrl)));
    }

    private Document getDocument(String authorization,
                                 String userId,
                                 CaseData caseData) {

        DocmosisTemplates templateType = getDocmosisTemplateType(caseData.getOrderTypeAndDocument().getType());

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
                service.getOrderTemplateData(caseData), templateType);

        OrderTypeAndDocument typeAndDoc = caseData.getOrderTypeAndDocument();
        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
            service.generateOrderDocumentFileName(typeAndDoc.getType(), typeAndDoc.getSubtype()));
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
}
