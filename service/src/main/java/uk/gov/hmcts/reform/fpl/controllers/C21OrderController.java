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
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CreateC21OrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C21;

@Api
@RequestMapping("/callback/create-order")
@RestController
public class C21OrderController {

    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService docmosisService;
    private final UploadDocumentService uploadDocumentService;
    private final CreateC21OrderService createC21OrderService;
    private final ValidateGroupService validateGroupService;

    @Autowired
    public C21OrderController(ObjectMapper mapper,
                              DocmosisDocumentGeneratorService docmosisService,
                              UploadDocumentService uploadDocumentService,
                              CreateC21OrderService createC21OrderService,
                              ValidateGroupService validateGroupService) {
        this.mapper = mapper;
        this.docmosisService = docmosisService;
        this.uploadDocumentService = uploadDocumentService;
        this.createC21OrderService = createC21OrderService;
        this.validateGroupService = validateGroupService;
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
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        String index = (caseData.getC21OrderBundle() != null)
            ? Integer.toString(caseData.getC21OrderBundle().size() + 1) : "1";


        Document c21Document = getDocument(
            authorization,
            userId,
            index,
            createC21OrderService.getC21OrderTemplateData(caseData));

        data.put("temporaryC21Order", addDocumentToC21Order(caseData, c21Document));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data).build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("c21OrderBundle", createC21OrderService.appendToC21OrderBundle(
            caseData.getTemporaryC21Order(), caseData.getC21OrderBundle(), caseData.getJudgeAndLegalAdvisor()));
        data.remove("temporaryC21Order");
        data.remove("judgeAndLegalAdvisor");

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }

    private C21Order addDocumentToC21Order(CaseData caseData, Document document) {
        return caseData.getTemporaryC21Order().toBuilder()
            .orderTitle(defaultIfBlank(caseData.getTemporaryC21Order().getOrderTitle(), "Order"))
            .c21OrderDocument(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .build();
    }

    private Document getDocument(@RequestHeader("authorization") String authorization,
                                 @RequestHeader("user-id") String userId,
                                 String index,
                                 Map<String, Object> templateData) {
        DocmosisDocument document = docmosisService.generateDocmosisDocument(templateData, C21);
        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
            C21.getDocumentTitle() + "_" + index + ".pdf");
    }
}
