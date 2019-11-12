package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.interfaces.C21CaseOrderGroup;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CreateC21OrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C21;

@Api
@RequestMapping("/callback/create-order")
@RestController
public class C21OrderController {
    private final ObjectMapper mapper;
    private final CreateC21OrderService service;
    private final ValidateGroupService validateGroupService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    @Autowired
    public C21OrderController(ObjectMapper mapper,
                              CreateC21OrderService service,
                              ValidateGroupService validateGroupService,
                              DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                              UploadDocumentService uploadDocumentService) {
        this.mapper = mapper;
        this.service = service;
        this.validateGroupService = validateGroupService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
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

        caseDetails.getData().put("c21Order", service.addDocumentToC21(caseData.getC21Order(), c21Document));

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

        c21Orders.add(service.addCustomValuesToC21Order(caseData.getC21Order(), caseData.getJudgeAndLegalAdvisor()));

        caseDetails.getData().put("c21Orders", c21Orders);
        caseDetails.getData().remove("c21Order");
        caseDetails.getData().remove("judgeAndLegalAdvisor");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    public Document getDocument(@RequestHeader("authorization") String authorization,
                                @RequestHeader("user-id") String userId,
                                CaseData caseData) {
        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(
            service.getC21OrderTemplateData(caseData), C21);

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
            C21.getDocumentTitle() + service.getIndexForC21Document(caseData.getC21Orders()) + ".pdf");
    }
}
