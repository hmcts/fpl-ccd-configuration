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
import uk.gov.hmcts.reform.fpl.model.C21OrderAnswers;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.CreateC21OrderService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
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

        // Append doc to check your answers

        Document c21Document = getDocument(
            authorization,
            userId,
            createC21OrderService.getC21OrderTemplateData(caseData));

        data.put("temporaryC21Order", buildTemporaryC21Order(caseData, c21Document));
        data.put("c21OrderAnswers", buildC21OrderAnswers(caseData, c21Document));
        data.remove("temporaryC21Order");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data).build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        Document c21Document = getDocument(authorization, userId,
            createC21OrderService.getC21OrderTemplateData(caseData));

        data.put("c21OrderBundle", createC21OrderService.appendToC21OrderBundle(
            buildTemporaryC21Order(caseData, c21Document), caseData.getC21OrderBundle()));
        data.remove("temporaryC21Order");
        data.remove("judgeAndLegalAdvisor");


        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }

    private C21Order buildTemporaryC21Order(CaseData caseData, Document document) {
        return caseData.getTemporaryC21Order().toBuilder()
            .orderTitle(defaultIfBlank(caseData.getTemporaryC21Order().getOrderTitle(), "Order"))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(caseData.getJudgeAndLegalAdvisor().getJudgeTitle())
                .judgeLastName(caseData.getJudgeAndLegalAdvisor().getJudgeLastName())
                .judgeFullName(caseData.getJudgeAndLegalAdvisor().getJudgeFullName())
                .legalAdvisorName(caseData.getJudgeAndLegalAdvisor().getLegalAdvisorName())
                .build())
            .c21OrderDocument(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .build();
    }

    private C21OrderAnswers buildC21OrderAnswers(CaseData caseData, Document document) {
        return C21OrderAnswers.builder()
            .orderTitle(defaultIfBlank(caseData.getTemporaryC21Order().getOrderTitle(), "Order"))
            .orderDetails(caseData.getTemporaryC21Order().getOrderDetails())
            .c21OrderDocument(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .judgeTitleAndName(getJudgeTitleAndName(caseData.getJudgeAndLegalAdvisor()))
            .legalAdvisor(defaultIfBlank(caseData.getJudgeAndLegalAdvisor().getLegalAdvisorName(), null))
            .build();
    }

    private Document getDocument(@RequestHeader("authorization") String authorization,
                                 @RequestHeader("user-id") String userId,
                                 Map<String, Object> templateData) {
        DocmosisDocument document = docmosisService.generateDocmosisDocument(templateData, C21);
        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), C21.getDocumentTitle());
    }

    private String getJudgeTitleAndName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        String judgeOrMagistrate = defaultIfEmpty(
            judgeAndLegalAdvisor.getJudgeLastName(),
            defaultIfEmpty(judgeAndLegalAdvisor.getJudgeFullName(), ""));

        String judgeTitle = "";

        if (judgeAndLegalAdvisor.getJudgeTitle() != null) {
            judgeTitle = judgeAndLegalAdvisor.getJudgeTitle().getLabel();
        }

        return defaultIfBlank(judgeTitle + " " + judgeOrMagistrate, "");
    }
}
