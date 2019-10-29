package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.assertj.core.util.Lists;
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
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.interfaces.C21CaseOrderGroup;
import uk.gov.hmcts.reform.fpl.model.C21Order;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C21OrderBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.CreateC21OrderService;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C21;

@Api
@RequestMapping("/callback/create-order")
@RestController
public class C21OrderController {

    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService docmosisService;
    private final UploadDocumentService uploadDocumentService;
    private final CreateC21OrderService createC21OrderService;
    private final DateFormatterService dateFormatterService;
    private final ValidateGroupService validateGroupService;

    @Autowired
    public C21OrderController(ObjectMapper mapper,
                              DocmosisDocumentGeneratorService docmosisService,
                              UploadDocumentService uploadDocumentService,
                              CreateC21OrderService createC21OrderService,
                              DateFormatterService dateFormatterService,
                              ValidateGroupService validateGroupService) {
        this.mapper = mapper;
        this.docmosisService = docmosisService;
        this.uploadDocumentService = uploadDocumentService;
        this.createC21OrderService = createC21OrderService;
        this.dateFormatterService = dateFormatterService;
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

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        Document c21Document = getDocument(
            authorization,
            userId,
            createC21OrderService.getC21OrderTemplateData(caseData));

        C21Order.C21OrderBuilder c21OrderBuilder = caseData.getTemporaryC21Order().toBuilder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(caseData.getJudgeAndLegalAdvisor().getJudgeTitle())
                .judgeLastName(caseData.getJudgeAndLegalAdvisor().getJudgeLastName())
                .judgeFullName(caseData.getJudgeAndLegalAdvisor().getJudgeFullName())
                .legalAdvisorName(caseData.getJudgeAndLegalAdvisor().getLegalAdvisorName())
                .build())
            .c21OrderDocument(DocumentReference.builder()
                .url(c21Document.links.self.href)
                .binaryUrl(c21Document.links.binary.href)
                .filename(c21Document.originalDocumentName)
                .build());

        data.put("temporaryC21Order", c21OrderBuilder.build());
        caseData = mapper.convertValue(data, CaseData.class);

        data.put("c21OrderBundle", buildC21OrderBundle(caseData));
        data.remove("temporaryC21Order");
        data.remove("judgeAndLegalAdvisor");

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }

    private List<Element<C21OrderBundle>> buildC21OrderBundle(CaseData caseData) {
        List<Element<C21OrderBundle>> c21OrderBundle = defaultIfNull(caseData.getC21OrderBundle(),
            Lists.newArrayList());

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        C21Order tempC21 = caseData.getTemporaryC21Order();
        JudgeOrMagistrateTitle judgeTitle = tempC21.getJudgeAndLegalAdvisor().getJudgeTitle();
        String judgeLabel;
        if (judgeTitle != null) {
            judgeLabel = tempC21.getJudgeAndLegalAdvisor().getJudgeTitle().getLabel();
        } else {
            judgeLabel = "";
        }

        c21OrderBundle.add(Element.<C21OrderBundle>builder()
            .id(UUID.randomUUID())
            .value(C21OrderBundle.builder()
                .orderTitle(tempC21.getOrderTitle())
                .c21OrderDocument(tempC21.getC21OrderDocument())
                .orderDate(dateFormatterService.formatLocalDateTimeBaseUsingFormat(zonedDateTime
                    .toLocalDateTime(), "h:mma, d MMMM yyyy"))
                .judgeTitle(judgeLabel)
                .judgeName(defaultIfNull(tempC21.getJudgeAndLegalAdvisor().getJudgeLastName(),
                    tempC21.getJudgeAndLegalAdvisor().getJudgeFullName()))
                .build())
            .build());

        return c21OrderBundle;
    }

    private Document getDocument(@RequestHeader("authorization") String authorization,
                                 @RequestHeader("user-id") String userId,
                                 Map<String, Object> templateData) {
        DocmosisDocument document = docmosisService.generateDocmosisDocument(templateData, C21);

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
            C21.getDocumentTitle() + ".pdf");
    }
}
