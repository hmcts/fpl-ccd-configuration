package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.events.C21OrderEvent;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CreateC21OrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.C21CaseOrderGroup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C21;

@Slf4j
@Api
@RequestMapping("/callback/create-order")
@RestController
public class C21OrderController {
    private final ObjectMapper mapper;
    private final CreateC21OrderService service;
    private final ValidateGroupService validateGroupService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final GatewayConfiguration gatewayConfiguration;

    @Autowired
    public C21OrderController(ObjectMapper mapper,
                              CreateC21OrderService service,
                              ValidateGroupService validateGroupService,
                              DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                              UploadDocumentService uploadDocumentService,
                              ApplicationEventPublisher applicationEventPublisher,
                              GatewayConfiguration gatewayConfiguration) {
        this.mapper = mapper;
        this.service = service;
        this.validateGroupService = validateGroupService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.gatewayConfiguration = gatewayConfiguration;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateGroupService.validateGroup(caseData, C21CaseOrderGroup.class))
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Document c21Document = getDocument(authorization, userId, caseData);

        //Update orderTypeAndDocument with the document so it can be displayed in check-your-answers
        caseDetails.getData().put("orderTypeAndDocument",
            service.updateTypeAndDocument(caseData.getOrderTypeAndDocument(), c21Document));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<C21Order>> c21Orders = caseData.getC21Orders();

        //Builds an order with all necessary values and adds it to list of orders
        c21Orders.add(service.addCustomValuesToC21Order(caseData.getC21Order(), caseData.getOrderTypeAndDocument(),
            caseData.getJudgeAndLegalAdvisor()));

        caseDetails.getData().put("c21Orders", c21Orders);
        caseDetails.getData().remove("c21Order");
        caseDetails.getData().remove("orderTypeAndDocument");
        caseDetails.getData().remove("judgeAndLegalAdvisor");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestHeader(value = "authorization") String authorization,
                                     @RequestHeader(value = "user-id") String userId,
                                     @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        String mostRecentUploadedDocumentUrl = service.mostRecentUploadedC21DocumentUrl(caseData.getC21Orders());

        applicationEventPublisher.publishEvent(new C21OrderEvent(callbackRequest, authorization, userId,
            concatGatewayConfigurationUrlAndMostRecentUploadedC21DocumentPath(mostRecentUploadedDocumentUrl)));
    }

    private Document getDocument(String authorization,
                                 String userId,
                                 CaseData caseData) {
        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            service.getC21OrderTemplateData(caseData), C21);

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
            C21.getDocumentTitle());
    }

    private String concatGatewayConfigurationUrlAndMostRecentUploadedC21DocumentPath(
        final String mostRecentUploadedC21DocumentUrl) {
        final String gatewayUrl = gatewayConfiguration.getUrl();

        try {
            URI uri = new URI(mostRecentUploadedC21DocumentUrl);
            return gatewayUrl + uri.getPath();
        } catch (URISyntaxException e) {
            log.error(mostRecentUploadedC21DocumentUrl + " url incorrect.", e);
        }
        return "";
    }
}
